package concurrent.vertex

import Dsssp.Companion.INF
import concurrent.process.Process
import concurrent.process.Status

data class Edge(val process: Process, val prev: Double, val cur: Double) {
    fun read(): Double {
        return when (process.status.value) {
            Status.SUCCESS -> cur
            else -> prev
        }
    }
}