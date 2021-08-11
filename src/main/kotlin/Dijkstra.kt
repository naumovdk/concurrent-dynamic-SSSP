import java.util.*
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.math.min

class Dijkstra(
    private val graph: Graph = mutableMapOf(
        0 to mutableMapOf(),
        1 to mutableMapOf(),
        2 to mutableMapOf(),
        3 to mutableMapOf()
    ), private val source: Int = 0
) : Dsssp() {
    private var changed = true
    private val distances = mutableMapOf<Int, Double>()

    @Synchronized
    override fun getDistance(index: Int): Double? {
        if (changed) {
            recompute()
        }
        return distances[index]
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
            graph[cur]!!.forEach { (u, w) ->
                distances[u] = min(distances[cur]!! + w, distances.getOrDefault(u, POSITIVE_INFINITY))
                if (!marked.contains(u)) {
                    marked.add(u)
                    pq.add(u)
                }
            }
        }
    }

    @Synchronized
    override fun setEdge(from: Int, to: Int, newWeight: Double): Boolean {
        if (!graph.containsKey(from)) {
            return false
        }
        graph[from]!![to] = newWeight
        changed = true
        return true
    }


    @Synchronized
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
}