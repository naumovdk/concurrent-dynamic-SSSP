package concurrent

class QueuedVertex(val vertex: Vertex, val distance: Distance) : Comparable<QueuedVertex> {
    override fun compareTo(other: QueuedVertex): Int {
        return compareValues(this.distance, other.distance)
    }
}
