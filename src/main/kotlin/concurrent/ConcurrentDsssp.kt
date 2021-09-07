package concurrent

import Dsssp
import concurrent.Status.ABORTED
import concurrent.Status.SUCCESS
import java.util.concurrent.ConcurrentHashMap

class ConcurrentDsssp(source: Int = 0) : Dsssp {
    private val vertexes = ConcurrentHashMap<Int, Vertex>()

    init {
        vertexes[source] = Vertex(0.0)

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
            val global = GlobalDescriptor(
                priority = threadId
            )
            global.from = from
            global.to = to
            global.newWeight = newWeight
            if (setEdgeOrAbort(from, to, newWeight, global)) {
                return true
            }
            assert(global.status.get() == ABORTED)
        }
    }

    private fun setEdgeOrAbort(from: Vertex, to: Vertex, newWeight: Double, global: GlobalDescriptor): Boolean {
        val new = Edge(newWeight, global.status)
        val existing = from.outgoing.getOrPut(to) { new }
        if (existing !== new) {
            while (true) {
                if (existing.set(newWeight, global.status)) {
                    break
                }
            }
        }

        from.acquire(null, global) ?: return false

        global.help()
        return global.status.get() == SUCCESS
    }


    override fun addVertex(index: Int): Boolean {
        val newVertex = Vertex()
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