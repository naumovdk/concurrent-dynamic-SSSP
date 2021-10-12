package sequential

import Dsssp
import INITIAL_SIZE
import java.util.*
import kotlin.Double.Companion.POSITIVE_INFINITY


class DijkstraRecomputing(private val source: Int = 0) : Dsssp {
    private val graph: MutableMap<Int, MutableMap<Int, Double>> = mutableMapOf()
    private val distances = mutableMapOf<Int, Double>()

    init {
        graph[source] = mutableMapOf()

        for (i in 0..INITIAL_SIZE) {
            addVertex(i)
        }
    }

    @Synchronized
    override fun getDistance(index: Int): Double? {
        recompute()
        return distances[index]
    }

    @Synchronized
    private fun recompute() {
        distances.forEach { (k, _) ->
            distances[k] = Dsssp.INF
        }
        distances[source] = 0.0

        val pq = PriorityQueue(compareBy<Int> { distances.getOrDefault(it, POSITIVE_INFINITY) })
        pq.add(source)

        while (pq.isNotEmpty()) {
            val cur = pq.poll()
            for ((index, w) in graph[cur]!!) {
                val offeredDistance = distances[cur]!! + w
                val curDistance = distances.getOrDefault(index, Dsssp.INF)
                if (offeredDistance < curDistance) {
                    distances[index] = offeredDistance
                    pq.filter { it != index }
                    pq.add(index)
                }
            }
        }
    }

    @Synchronized
    override fun setEdge(fromIndex: Int, toIndex: Int, newWeight: Double): Boolean {
        if (fromIndex == toIndex) return false
        if (!graph.containsKey(fromIndex)) return false
        if (!graph.containsKey(toIndex)) return false

        graph[fromIndex]!![toIndex] = newWeight
        return true
    }

    override fun removeEdge(fromIndex: Int, toIndex: Int): Boolean {
        if (!graph.containsKey(fromIndex)) return false
        if (!graph[fromIndex]!!.containsKey(toIndex)) return false

        graph[fromIndex]!!.remove(toIndex)
        return true
    }

    @Synchronized
    override fun addVertex(index: Int): Boolean {
        if (graph.containsKey(index)) return false

        graph[index] = mutableMapOf()
        distances[index] = Dsssp.INF
        return true
    }

    override fun removeVertex(index: Int): Boolean {
        if (!graph.containsKey(index)) {
            return false
        }
        graph.remove(index)
        return true
    }

    override fun getAllDistances(): Map<Int, Double> {
        TODO("Not yet implemented")
    }
}