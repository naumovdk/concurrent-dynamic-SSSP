import java.util.*
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.math.min

class Dijkstra(private val graph: Graph = mutableMapOf(), private val source: Int = 0) : Dsssp() {
    private var changed = true
    private val distances = mutableMapOf<Int, Double>()

    init {
        graph[source] = mutableMapOf()

        for (i in 0..INITIAL_GRAPH_SIZE) {
            addVertex(i)
        }
    }

    @Synchronized
    override fun getDistance(index: Int): Double? {
        if (!graph.contains(index)) {
            return null
        }
        if (changed) {
            recompute()
        }
        return distances.getOrDefault(index, POSITIVE_INFINITY)
    }

    @Synchronized
    private fun recompute() {
        val pq = PriorityQueue(compareBy<Int> { distances.getOrDefault(it, POSITIVE_INFINITY) })
        pq.add(source)

        val marked = mutableSetOf<Int>()
        marked.add(source)

        distances.clear()
        distances[source] = 0.0

        while (pq.isNotEmpty()) {
            val cur = pq.poll()
            for ((u, w) in graph[cur]!!) {
                distances[u] = min(distances[cur]!! + w, distances.getOrDefault(u, POSITIVE_INFINITY))
                if (!marked.contains(u)) {
                    marked.add(u)
                    pq.add(u)
                }
            }
        }
    }

    @Synchronized
    override fun setEdge(fromIndex: Int, toIndex: Int, newWeight: Double): Boolean {
        if (fromIndex == toIndex) return false
        if (!graph.containsKey(fromIndex)) {
            return false
        }
        if (!graph.containsKey(toIndex)) {
            return false
        }
        if (changed) {
            recompute()
        }
        if (distances.getOrDefault(toIndex, POSITIVE_INFINITY) <= distances.getOrDefault(fromIndex, POSITIVE_INFINITY) + newWeight) {
            return false
        }
        graph[fromIndex]!![toIndex] = newWeight
        changed = true
        return true
    }


    @Synchronized
    override fun removeEdge(fromIndex: Int, toIndex: Int): Boolean {
        if (!graph.containsKey(fromIndex)) {
            return false
        }
        if (!graph[fromIndex]!!.containsKey(toIndex)) {
            return false
        }
        graph[fromIndex]!!.remove(toIndex)
        changed = true
        return true
    }


    @Synchronized
    override fun addVertex(index: Int): Boolean {
        if (graph.containsKey(index)) {
            return false
        }
        graph[index] = mutableMapOf()
        return true
    }

    @Synchronized
    override fun removeVertex(index: Int): Boolean {
        if (!graph.containsKey(index)) {
            return false
        }
        graph.remove(index)
        return true
    }

    fun extractState(): Any {
        return this
    }
}