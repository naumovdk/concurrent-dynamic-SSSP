const val INITIAL_SIZE = 100

interface Dsssp {
    fun getDistance(index: Int): Double?

    fun setEdge(fromIndex: Int, toIndex: Int, newWeight: Double): Boolean

    fun removeEdge(fromIndex: Int, toIndex: Int): Boolean

    fun addVertex(index: Int): Boolean

    fun removeVertex(index: Int): Boolean

    fun getAllDistances(): Map<Int, Double>

    companion object {
        val INF: Double
            get() = Double.POSITIVE_INFINITY

        const val supportDec = true
        const val supportInc = true
        const val supportHelp = true
    }
}