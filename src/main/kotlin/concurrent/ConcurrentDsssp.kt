package concurrent

import concurrent.Status.*
import Dsssp
import java.util.concurrent.ConcurrentHashMap


class ConcurrentDsssp(source: Int = 0) : Dsssp {
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
        val fromDistance = from.acquire(null, global)

        val offeredDistance = Distance(fromDistance.value + newWeight, from)
        val oldDistance = to.acquire(offeredDistance, global)

        // workaround
        if (global.status.get() == ABORTED) {
            return false
        }

        // add edge

        global.help()

        return global.status.get() == SUCCESS
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