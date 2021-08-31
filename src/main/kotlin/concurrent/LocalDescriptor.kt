package concurrent

class LocalDescriptor(
    val newDist: Distance,
    val oldDist: Distance,
    val global: GlobalDescriptor
) {
    fun readDistance(status: Status): Distance {
        return when (status) {
            Status.SUCCESS -> newDist
            Status.IN_PROGRESS -> oldDist
            Status.ABORTED -> oldDist
        }
    }
}