package concurrent.vertex

import concurrent.process.Process
import concurrent.process.Status

data class Edge(val process: Process, val prev: Double, val cur: Double) {
    fun read(reader: Process): Double {
        return when (process.status.value) {
            Status.SUCCESS -> cur
            else -> when (reader) {
                process -> cur
                else -> prev
            }
        }
    }
}