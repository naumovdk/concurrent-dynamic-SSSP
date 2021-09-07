package concurrent

import concurrent.Status.*
import java.util.concurrent.atomic.AtomicReference


class Edge(weight: Double, status: AtomicReference<Status>) {
    data class EdgeType(val weight: Double, val status: AtomicReference<Status>)

    var oldEdge: EdgeType = EdgeType(Double.POSITIVE_INFINITY, AtomicReference(SUCCESS))
    var newEdge: EdgeType = EdgeType(weight, status)

    fun readWeight(): Double {
        return if (newEdge.status.get() == SUCCESS) {
            newEdge.weight
        } else {
            oldEdge.weight
        }
    }

    fun set(weight: Double, status: AtomicReference<Status>): Boolean { // todo
        return when (newEdge.status.get()) {
            SUCCESS -> {
                oldEdge = newEdge
                newEdge = EdgeType(weight, status)
                true
            }
            ABORTED -> {
                newEdge = EdgeType(weight, status)
                true
            }
            IN_PROGRESS -> {
                false
            }
        }
    }
}