import java.util.*
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.math.min

class Dijkstra(private val graph: Graph, private val source: Int) : Dsssp(graph, source) {
    private var changed = true
    private val distances = mutableMapOf<Int, Double>()

    override fun getDistance(vertex: Int): Double? {
        if (changed) {
            recompute()
        }
        return distances[vertex]
    }

    private fun recompute() {
        val pq = PriorityQueue(compareBy<Int> { distances.getOrDefault(it, POSITIVE_INFINITY) })
        pq.add(source)

        val marked = mutableSetOf<Int>()
        marked.add(source)

        distances.clear()
        distances[source] = 0.0

        while (pq.isNotEmpty()) {
            val cur = pq.poll()
            graph[cur]!!.forEach { (u, w) ->
                distances[u] = min(distances[cur]!! + w, distances.getOrDefault(u, POSITIVE_INFINITY))
                if (!marked.contains(u)) {
                    marked.add(u)
                    pq.add(u)
                }
            }
        }
    }

    override fun setEdge(from: Int, to: Int, newWeight: Double) {
        if (!graph.containsKey(from)) {
            graph[from] = mutableMapOf()
        }
        graph[from]!![to] = newWeight
        changed = true
    }

    override fun removeEdge(from: Int, to: Int): Boolean {
        if (!graph.containsKey(from)) {
            return false
        }
        if (!graph[from]!!.containsKey(to)) {
            return false
        }
        graph[from]!!.remove(to)
        changed = true
        return true
    }

    override fun addVertex(vertex: Int): Boolean {
        if (graph.containsKey(vertex)) {
            return false
        }
        graph[vertex] = mutableMapOf()
        return true
    }

    override fun removeVertex(vertex: Int): Boolean {
        if (!graph.containsKey(vertex)) {
            return false
        }
        graph.remove(vertex)
        return true
    }
}