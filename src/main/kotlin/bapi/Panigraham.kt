package bapi

import Dsssp
import INITIAL_SIZE
import InputGraph
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

class Panigraham(inputGraph: InputGraph, private val source: Int = 0) : Dsssp(inputGraph) {
    private val vertexes = ConcurrentHashMap<Int, Vertex>()

    init {
        for (i in 0..INITIAL_SIZE) {
            assert(addVertex(i))
        }
    }

    override fun addVertex(index: Int): Boolean {
        val newVertex = Vertex(index)
        val mapped = vertexes.getOrPut(index) { newVertex }
        return mapped === newVertex

    }

    override fun getDistance(index: Int): Int? {
        return (sssp()[index] ?: Dsssp.INF) as Int?
    }

    private fun sssp(): Map<Int, Double> {
        var prev = ssspTree()
        while (true) {
            val cur = ssspTree()
            if (prev == cur) {
                return cur
            }
            prev = cur
        }
    }

    private fun ssspTree(): Map<Int, Double> {
        val distances = HashMap<Int, Double>()
        distances[0] = 0.0

        val priorityQueue = PriorityQueue<Pair<Double, Vertex>>(compareBy { it.first })
        priorityQueue.add(0.0 to vertexes[source]!!)

        while (priorityQueue.isNotEmpty()) {
            val (curDist, cur) = priorityQueue.poll() ?: continue
            for ((u, w) in cur.outgoing) {
                val uDist = distances[u.index] ?: Dsssp.INF
                val offeredDist = curDist + w
                if (offeredDist < uDist) {
                    distances[u.index] = offeredDist
                    priorityQueue.add(offeredDist to u)
                }
            }
        }

        return distances
    }

    override fun setEdge(fromIndex: Int, toIndex: Int, newWeight: Double): Boolean {
        if (fromIndex == toIndex) return false
        val from = vertexes[fromIndex] ?: return false
        val to = vertexes[toIndex] ?: return false

        from.outgoing.putIfAbsent(to, newWeight)
        from.outgoing.replace(to, newWeight)
        return true
    }

    override fun removeEdge(fromIndex: Int, toIndex: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeVertex(index: Int): Boolean {
        TODO("Not yet implemented")
    }
}