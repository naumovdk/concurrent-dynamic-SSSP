import DssspImpl.Status.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.Double.Companion.POSITIVE_INFINITY


class DssspImpl(source: Int = 0) : Dsssp {
    private val vertexes = ConcurrentHashMap<Int, Vertex>()

    enum class Status {
        SUCCESS, IN_PROGRESS, ABORTED
    }

    data class QueuedVertex(val vertex: Vertex, val priority: Double)

    class GlobalDesc(
        val priorityQueue: PriorityQueue<QueuedVertex> = PriorityQueue(compareBy { it.priority }),
        val status: AtomicReference<Status> = AtomicReference(IN_PROGRESS) // maybe change ref to volatile
    )

    class Distance(
        val value: Double,
        val parent: Vertex?
    )

    class LocalDesc(
        private val newDist: Distance,
        private val oldDist: Distance,
        val global: GlobalDesc
    ) {
        fun readDistance(status: Status): Distance {
            return when (status) {
                SUCCESS -> newDist
                IN_PROGRESS -> oldDist
                ABORTED -> oldDist
            }
        }

        fun readDistanceImTheOwner(owner: GlobalDesc): Distance {
            assert(global === owner)
            assert(global.status.get() == IN_PROGRESS)
            return newDist
        }
    }

    class Vertex(val index: Int, distance: Double = POSITIVE_INFINITY) {
        val outgoing = ConcurrentHashMap<Int, Double>()
        val local: AtomicReference<LocalDesc> = AtomicReference(
            LocalDesc(
                Distance(distance, null),
                Distance(POSITIVE_INFINITY, null),
                GlobalDesc(PriorityQueue(), AtomicReference(SUCCESS))
            )
        )

        fun acquireIfDecrement(newDist: Distance, global: GlobalDesc): Boolean? {
            assert(global.status.get() == IN_PROGRESS)
            while (true) {
                val cur = local.get()
                val curStatus = cur.global.status.get()
                if (curStatus != IN_PROGRESS || global == cur.global) {
                    val curDist =
                        if (global === cur.global) cur.readDistanceImTheOwner(global) else getLocalAndReadDist()
                    if (curDist.value <= newDist.value) {
                        return false
                    }
                    val new = LocalDesc(newDist, curDist, global)
                    if (local.compareAndSet(cur, new)) {
                        return true
                    }
                } else {
                    return null
                }
            }
        }

        fun getLocalAndReadDist(): Distance {
            while (true) {
                val cur = local.get()
                val status = cur.global.status.get()
                if (cur === local.get()) {
                    return cur.readDistance(status)
                }
            }
        }
    }

    init {
        vertexes[source] = Vertex(source, 0.0)

        for (i in 0..INITIAL_GRAPH_SIZE) {
            addVertex(i)
        }
    }

    override fun getDistance(index: Int): Double? {
        val vertex = vertexes[index] ?: return null
        return vertex.getLocalAndReadDist().value
    }

    override fun setEdge(fromIndex: Int, toIndex: Int, newWeight: Double): Boolean {
        if (fromIndex == toIndex) return false
        val from = vertexes[fromIndex] ?: return false
        val to = vertexes[toIndex] ?: return false

        while (true) {
            val global = GlobalDesc()
            if (setEdgeOrAbort(from, to, newWeight, global)) {
                return true
            }
            assert(global.status.compareAndSet(IN_PROGRESS, ABORTED))
        }
    }

    private fun setEdgeOrAbort(from: Vertex, to: Vertex, newWeight: Double, global: GlobalDesc): Boolean {
        var fromDist: Distance
        while (true) {
            val cur = from.local.get()
            val status = cur.global.status.get()
            if (status != IN_PROGRESS) {
                fromDist = cur.readDistance(status)
                val new = LocalDesc(newDist = fromDist, oldDist = fromDist, global = global)
                if (from.local.compareAndSet(cur, new)) {
                    break
                }
            }
        }

        val offeredDist = Distance(fromDist.value + newWeight, from)
        var oldDist: Distance
        while (true) {
            val cur = to.local.get()
            val status = cur.global.status.get()
            if (status != IN_PROGRESS) {
                oldDist = cur.readDistance(status)
                val new = LocalDesc(newDist = offeredDist, oldDist = oldDist, global = global)
                if (to.local.compareAndSet(cur, new)) {
                    break
                }
            } else {
                return false
            }
        }

        from.outgoing[to.index] = newWeight

        if (offeredDist.value < oldDist.value) {
            // dec
            global.priorityQueue.add(QueuedVertex(to, fromDist.value + newWeight))
            while (global.priorityQueue.isNotEmpty()) {
                val cur = global.priorityQueue.poll().vertex
                val curDist = cur.local.get().readDistanceImTheOwner(global)
                cur.outgoing.forEach { (i, w) ->
                    val neighbor = vertexes[i]!!
                    val newDist = Distance(curDist.value + w, cur)
                    if (neighbor.acquireIfDecrement(newDist, global) ?: run { return false }) {
                        global.priorityQueue.add(QueuedVertex(neighbor, newDist.value))
                    }
                }
            }
            assert(global.status.compareAndSet(IN_PROGRESS, SUCCESS))
        } else if (oldDist.value < offeredDist.value) {
            if (oldDist.parent === from) {
                // inc
                assert(global.status.compareAndSet(IN_PROGRESS, ABORTED))
                throw IncrementalIsNotSupportedException()
            }
            assert(global.status.compareAndSet(IN_PROGRESS, ABORTED))
        } else {
            assert(global.status.compareAndSet(IN_PROGRESS, SUCCESS))
        }

        return true
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