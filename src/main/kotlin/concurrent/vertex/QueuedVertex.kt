package concurrent.vertex

data class QueuedVertex(val vertex: Vertex, val distance: Double) : Comparable<QueuedVertex> {
    override fun compareTo(other: QueuedVertex): Int {
        return compareValues(this.distance, other.distance)
    }
}
