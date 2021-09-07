package concurrent

import concurrent.Status.*
import java.lang.AssertionError

class LocalDescriptor(
    val newDistance: Distance,
    val oldDistance: Distance,
    val global: GlobalDescriptor
) {
    fun readDistance(status: Status): Distance {
        return when(status) {
            SUCCESS -> newDistance
            ABORTED -> oldDistance
            else -> throw AssertionError()
        }
    }
}