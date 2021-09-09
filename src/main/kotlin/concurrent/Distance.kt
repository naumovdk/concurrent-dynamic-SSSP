package concurrent

class Distance(val value: Double, val parent: Vertex?) : Comparable<Distance> {
    override fun compareTo(other: Distance): Int {
        return compareValues(this.value, other.value)
    }

    companion object {
        val INF = Distance(Dsssp.INF, null)
    }
}