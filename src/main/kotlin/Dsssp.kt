const val INITIAL_SIZE = 0

interface Dsssp {

    fun getDistance(index: Int): Double?

    fun setEdge(fromIndex: Int, toIndex: Int, newWeight: Double): Boolean

    fun removeEdge(fromIndex: Int, toIndex: Int): Boolean

    fun addVertex(index: Int): Boolean

    fun removeVertex(index: Int): Boolean

    companion object {
        val INF: Double
            get() = Double.POSITIVE_INFINITY

        const val supportDecremental = true
        const val supportIncremental = true
        const val supportHelp = true
    }
}

fun Dsssp.fit(inputGraph: InputGraph) {
    for (e in inputGraph.edges) {
        val (u, v, w) = e
        this.addVertex(u)
        this.addVertex(v)
        this.setEdge(u, v, w.toDouble())
    }
}