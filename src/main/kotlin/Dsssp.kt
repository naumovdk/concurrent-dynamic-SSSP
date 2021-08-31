import org.jetbrains.kotlinx.lincheck.verifier.VerifierState

typealias Graph = MutableMap<Int, MutableMap<Int, Double>>

interface Dsssp {
    val INITIAL_GRAPH_SIZE: Int
        get() = 7

    fun getDistance(index: Int) : Double?

    fun setEdge(fromIndex: Int, toIndex: Int, newWeight: Double): Boolean

    fun removeEdge(fromIndex: Int, toIndex: Int) : Boolean

    fun addVertex(index: Int) : Boolean

    fun removeVertex(index: Int) : Boolean
}