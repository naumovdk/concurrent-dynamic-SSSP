typealias Graph = MutableMap<Int, MutableMap<Int, Double>>

abstract class Dsssp(graph: Graph, source: Int) {
    fun decreaseWeight(from: Int, to: Int) {

    }

    abstract fun getDistance(vertex: Int) : Double?

    abstract fun setEdge(from: Int, to: Int, newWeight: Double)

    abstract fun removeEdge(from: Int, to: Int) : Boolean

    abstract fun addVertex(vertex: Int) : Boolean

    abstract fun removeVertex(vertex: Int) : Boolean
}