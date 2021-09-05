package concurrent

import concurrent.Status.*

class LocalDescriptor(
    val newDistance: Distance,
    val oldDistance: Distance,
    val global: GlobalDescriptor
) {
    fun readDistance(status: Status): Distance {
        return if (status == SUCCESS) newDistance else oldDistance
    }
}