package concurrent

import concurrent.Status.*
import kotlinx.atomicfu.atomic
import java.util.concurrent.PriorityBlockingQueue

class Process(
    private val priority: Long,
    private val newWeight: Double,
    status: Status = INITIALIZATION
) {
    val successfullySetEdge = atomic(false)
    lateinit var from: Vertex
    lateinit var to: Vertex
    lateinit var both: BothDescriptors

    val status = atomic(status)
    private val queue = PriorityBlockingQueue<QueuedVertex>(1)

    fun onIntersection(other: Process) {
        if (this.priority < priority) {
            other.help()
        } else {
            val curStatus = other.status.value
            if (curStatus.isInProgress()) {
                other.status.compareAndSet(curStatus, ABORTED)
            }
        }
    }

    fun help() {
        val fromDistance = from.acquire(null, both)
        val offeredDistance = Distance(fromDistance.value + newWeight, from)
        val toDistance = to.acquire(offeredDistance, both)

        if (!successfullySetEdge.value) {
            from.plantEdge(newWeight, to)
        }

        if (offeredDistance < fromDistance) {

        } else if (offeredDistance > fromDistance && offeredDistance.parent === toDistance.parent) {
            // incremental
        }

        val curStatus = status.value
        if (curStatus.isInProgress()) {
            status.compareAndSet(curStatus, SUCCESS)
        }
    }

    companion object {
        val UNINITIALIZED = Process(0, 0.0, SUCCESS)
    }
}