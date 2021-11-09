package concurrent.vertex

import Dsssp

data class Distance(val distance: Double, val parent: Vertex?) : Comparable<Distance> {
    override fun compareTo(other: Distance): Int {
        return compareValues(this.distance, other.distance)
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        var result = distance.hashCode()
        result = 31 * result + (parent?.hashCode() ?: 0)
        return result
    }

    companion object {
        val INF = Distance(Dsssp.INF, null)
    }
}