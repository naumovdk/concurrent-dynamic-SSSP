import org.jetbrains.kotlinx.lincheck.verifier.VerifierState
import java.util.*
import kotlin.Double.Companion.POSITIVE_INFINITY

class Dijkstra(source: Int = 0) : Dsssp, VerifierState() {
    class Vertex(
        var distance: Double = POSITIVE_INFINITY,
        val outgoing: HashMap<Int, Double> = hashMapOf(),
        var parent: Vertex? = null
    )

    private val vertexes = HashMap<Int, Vertex>()

    init {
        vertexes[source] = Vertex(0.0)

        for (i in 0..INITIAL_GRAPH_SIZE) {
            addVertex(i)
        }
    }

    override fun getDistance(index: Int): Double? {
        return vertexes[index]?.distance
    }

    override fun setEdge(fromIndex: Int, toIndex: Int, newWeight: Double): Boolean {
        if (fromIndex == toIndex) return false
        val from = vertexes[fromIndex] ?: return false
        val to = vertexes[toIndex] ?: return false

        val newDist = from.distance + newWeight

        if (to.distance < newDist && to.parent === from) {
            // inc
            throw IncrementalIsNotSupportedException()
        }
        if (to.distance > newDist) {
            // dec
            to.distance = newDist
            to.parent = from
            val priorityQueue = PriorityQueue<Vertex>(compareBy { it.distance })
            priorityQueue.add(to)

            while (priorityQueue.isNotEmpty()) {
                val cur = priorityQueue.poll()
                cur.outgoing.forEach { (i, w) ->
                    val neighbor = vertexes[i]!!
                    if (cur.distance + w < neighbor.distance) {
                        neighbor.parent = cur
                        neighbor.distance = cur.distance + w
                        // maybe delete neighbor?
                        priorityQueue.add(neighbor)
                    }
                }
            }
        }
        // to.dist = const
        from.outgoing[toIndex] = newWeight
        return true
    }


    override fun addVertex(index: Int): Boolean {
        val new = Vertex()
        val mapped = vertexes.getOrPut(index) { new }
        return new === mapped
    }

    override fun removeEdge(fromIndex: Int, toIndex: Int): Boolean {
        TODO()
    }

    override fun removeVertex(index: Int): Boolean {
        TODO()
    }

    override fun extractState(): Any {
        return vertexes.map { (i, v) -> i to v }
    }
}