import org.jetbrains.kotlinx.lincheck.verifier.VerifierState

typealias Graph = MutableMap<Int, MutableMap<Int, Double>>

abstract class Dsssp {
    abstract fun getDistance(index: Int) : Double?

    abstract fun setEdge(fromIndex: Int, toIndex: Int, newWeight: Double): Boolean

    abstract fun removeEdge(fromIndex: Int, toIndex: Int) : Boolean

    abstract fun addVertex(index: Int) : Boolean

    abstract fun removeVertex(index: Int) : Boolean
}