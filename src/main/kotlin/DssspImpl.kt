import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

class DssspImpl(private val graph: Graph, private val source: Int) : Dsssp(graph, source) {
    class Vertex(val index: Int) {
        val outgoing = ConcurrentSkipListSet<Edge>()
        var distance = Double.POSITIVE_INFINITY
        var operation: Function<Void>? = null
        var beingUpdated = false
    }

    data class Edge(val to: Vertex, val weight: Double)

    private val vertexes = ConcurrentHashMap<Int, Vertex>()

    init {
        vertexes[source] = Vertex(source)
        vertexes[source]!!.distance = 0.0
    }

    override fun getDistance(vertex: Int): Double? {
        TODO("Not yet implemented")
    }

    override fun setEdge(from: Int, to: Int, newWeight: Double) {
        TODO("Not yet implemented")
    }

    override fun removeEdge(from: Int, to: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun addVertex(vertex: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeVertex(vertex: Int): Boolean {
        TODO("Not yet implemented")
    }
}