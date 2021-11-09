package bapi

import java.util.concurrent.ConcurrentHashMap

data class Vertex(
    val index: Int,
    val outgoing: ConcurrentHashMap<Vertex, Double> = ConcurrentHashMap()
) {
    override fun equals(other: Any?): Boolean {
        if (other is Vertex) {
            return index == other.index
        }
        return false
    }

    override fun hashCode(): Int {
        return index
    }
}

data class Edge(val weight: Double, val vertex: Vertex)