package concurrent

import concurrent.Status.*
import Dsssp
import IncrementalIsNotSupportedException
import java.util.concurrent.ConcurrentHashMap




class Concurrent(source: Int = 0) : Dsssp {
    private val vertexes = ConcurrentHashMap<Int, Vertex>()

    init {
        vertexes[source] = Vertex(source, 0.0)

        for (i in 0..INITIAL_GRAPH_SIZE) {
            addVertex(i)
        }
    }

    override fun getDistance(index: Int): Double? {
        val vertex = vertexes[index] ?: return null
        return vertex.readDistance().value
    }

    override fun setEdge(fromIndex: Int, toIndex: Int, newWeight: Double): Boolean {
        if (fromIndex == toIndex) return false
        val from = vertexes[fromIndex] ?: return false
        val to = vertexes[toIndex] ?: return false

        while (true) {
            val threadId = Thread.currentThread().id
            val global = GlobalDescriptor(threadId)
            if (setEdgeOrAbort(from, to, newWeight, global)) {
                return true
            }
            assert(global.status.get() == ABORTED)
        }
    }

    private fun setEdgeOrAbort(from: Vertex, to: Vertex, newWeight: Double, global: GlobalDescriptor): Boolean {
        var fromDist: Distance
        while (true) {
            val cur = from.local.get()
            val status = cur.global.status.get()
            if (status != IN_PROGRESS) {
                fromDist = cur.readDistance(status)
                val new = LocalDescriptor(newDist = fromDist, oldDist = fromDist, global = global)
                if (from.local.compareAndSet(cur, new)) {
                    break
                }
            }
        }

        val offeredDist = Distance(fromDist.value + newWeight, from)
        var oldDist: Distance
        while (true) {
            if (global.status.get() == ABORTED) {
                return false
            }
            val cur = to.local.get()
            val status = cur.global.status.get()
            if (status != IN_PROGRESS) {
                oldDist = cur.readDistance(status)
                val new = LocalDescriptor(newDist = offeredDist, oldDist = oldDist, global = global)
                if (to.local.compareAndSet(cur, new)) {
                    break
                }
            } else {
                when {
                    global.priority > cur.global.priority -> {
                        cur.global.status.compareAndSet(IN_PROGRESS, ABORTED)
                    }
                    global.priority < cur.global.priority -> {
                        global.status.compareAndSet(IN_PROGRESS, ABORTED)
                        return false
                    }
                    else -> {
                        throw Exception("impossible, helping should be there")
                    }
                }
            }
        }

        // workaround
        if (global.status.get() == ABORTED) {
            return false
        }

        return when {
            offeredDist.value < oldDist.value -> {
                global.priorityQueue.add(QueuedVertex(to, fromDist.value + newWeight))
                while (global.priorityQueue.isNotEmpty()) {
                    val cur = global.priorityQueue.poll().vertex
                    val curDist = cur.local.get().newDist
                    cur.outgoing.forEach { (i, w) ->
                        val neighbor = vertexes[i]!!
                        val newDist = Distance(curDist.value + w, cur)
                        if (neighbor.acquireIfImproves(newDist, global) ?: run { return false }) {
                            global.priorityQueue.add(QueuedVertex(neighbor, newDist.value))
                        }
                    }
                }
                from.outgoing[to.index] = newWeight
                global.status.compareAndSet(IN_PROGRESS, SUCCESS)
            }
            oldDist.value < offeredDist.value -> {
                if (oldDist.parent === from) {
                    global.status.set(ABORTED)
                    throw IncrementalIsNotSupportedException()
                }
                from.outgoing[to.index] = newWeight
                global.status.compareAndSet(IN_PROGRESS, ABORTED)
            }
            else -> {
                from.outgoing[to.index] = newWeight
                global.status.compareAndSet(IN_PROGRESS, SUCCESS)
            }
        }
    }


    override fun addVertex(index: Int): Boolean {
        val newVertex = Vertex(index)
        val mapped = vertexes.getOrPut(index) { newVertex }
        return mapped === newVertex
    }

    override fun removeEdge(fromIndex: Int, toIndex: Int): Boolean {
        TODO()
    }

    override fun removeVertex(index: Int): Boolean {
        TODO()
    }
}