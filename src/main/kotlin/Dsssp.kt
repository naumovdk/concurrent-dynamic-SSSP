import org.jetbrains.kotlinx.lincheck.verifier.VerifierState

const val INITIAL_SIZE = 7

abstract class Dsssp : VerifierState() {
    private var inputGraph = InputGraph(0, listOf(), 0, 0)

    init {
        for (e in inputGraph.edges) {
            val (u, v, w) = e
            this.addVertex(u)
            this.addVertex(v)
            this.setEdge(u, v, w.toDouble())
        }
    }

    fun fit(inputGraph: InputGraph): Dsssp {
        for (e in inputGraph.edges) {
            val (u, v, w) = e
            this.addVertex(u)
            this.addVertex(v)
            this.setEdge(u, v, w.toDouble())
        }
        return this
    }

    abstract fun getDistance(index: Int): Double?

    abstract fun setEdge(fromIndex: Int, toIndex: Int, newWeight: Double): Boolean

    abstract fun removeEdge(fromIndex: Int, toIndex: Int): Boolean

    abstract fun addVertex(index: Int): Boolean

    abstract fun removeVertex(index: Int): Boolean

    companion object {
        val INF: Double
            get() = Double.POSITIVE_INFINITY

        const val supportDecremental = true
        const val supportIncremental = true
        const val supportHelp = true
    }
}