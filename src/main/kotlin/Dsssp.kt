const val INITIAL_SIZE = 100

abstract class Dsssp {
    private var inputGraph = InputGraph(0, listOf(), 0, 0)

    abstract fun getDistance(index: Int): Int?

    abstract fun setEdge(fromIndex: Int, toIndex: Int, newWeight: Double): Boolean

    abstract fun removeEdge(fromIndex: Int, toIndex: Int): Boolean

    abstract fun addVertex(index: Int): Boolean

    abstract fun removeVertex(index: Int): Boolean

    constructor()

    constructor(inputGraph: InputGraph) {
        this.inputGraph = inputGraph
    }

    init {
        for (e in inputGraph.edges) {
            val (u, v, w) = e
            this.addVertex(u)
            this.addVertex(v)
            this.setEdge(u, v, w.toDouble())
        }
    }

    companion object {
        val INF: Double
            get() = Double.POSITIVE_INFINITY

        const val supportDec = true
        const val supportInc = true
        const val supportHelp = true
    }
}