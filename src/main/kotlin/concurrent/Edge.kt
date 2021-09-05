package concurrent

import concurrent.Status.*
import java.util.concurrent.atomic.AtomicReference

class Edge(weight: Double, status: AtomicReference<Status>) {
    class Edges(weight: Double, status: AtomicReference<Status>) {
        data class Edge(val weight: Double, val status: AtomicReference<Status>)

        val oldEdge: Edge = Edge(Double.POSITIVE_INFINITY, AtomicReference(SUCCESS))
        val newEdge: Edge = Edge(weight, status)
    }

    private val edges = Edges(weight, status)

    private fun isNewSuccessful(): Boolean {
        return edges.newEdge.status.get() == IN_PROGRESS
    }

    fun readEdge(): Double {
        return if (isNewSuccessful()) {
            edges.newEdge.weight
        } else {
            edges.oldEdge.weight
        }
    }

    fun setEdge() {
        edges =  if (isNewSuccessful()) {

        }
    }
}