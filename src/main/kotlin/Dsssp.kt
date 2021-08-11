import org.jetbrains.kotlinx.lincheck.verifier.VerifierState

typealias Graph = MutableMap<Int, MutableMap<Int, Double>>

abstract class Dsssp {
    abstract fun getDistance(index: Int) : Double?

    abstract fun setEdge(from: Int, to: Int, newWeight: Double): Boolean

    abstract fun removeEdge(from: Int, to: Int) : Boolean

    abstract fun addVertex(index: Int) : Boolean

    abstract fun removeVertex(index: Int) : Boolean
}