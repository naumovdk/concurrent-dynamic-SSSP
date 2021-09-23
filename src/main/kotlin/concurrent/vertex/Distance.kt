package concurrent.vertex

import Dsssp

class Distance(val value: Double, val parent: Vertex?) : Comparable<Distance> {
    override fun compareTo(other: Distance): Int {
        return compareValues(this.value, other.value)
    }

    operator fun plus(weight: Double): Distance {
        return Distance(this.value + weight, parent)
    }

    companion object {
        val INF = Distance(Dsssp.INF, null)
    }
}