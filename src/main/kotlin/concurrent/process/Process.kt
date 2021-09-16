package concurrent.process

import concurrent.process.Status.*
import concurrent.vertex.Distance
import concurrent.vertex.QueuedVertex
import concurrent.vertex.Vertex
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

class Process(
    private val priority: Long,
    private val newWeight: Double,
    status: Status = INITIALIZATION
) {
    val successfullySetEdge = atomic(false)
    lateinit var from: Vertex
    lateinit var to: Vertex
    lateinit var both: BothDescriptors

    val fromReplace: AtomicRef<Double?> = atomic(null)
    val toReplace: AtomicRef<Double?> = atomic(null)

    val status = atomic(status)
    private val queue = atomic<PriorityBlockingQueue<QueuedVertex>?>(null)

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

    // todo проверки на ABORTED
    // todo lock free helping

    fun help() {
        val fromDistance = from.acquire(null, both)

        val offeredDistance = Distance(fromDistance.value + newWeight, from)
        val oldToDistance = to.acquire(offeredDistance, both)

        if (!successfullySetEdge.value) {
            from.plantEdge(newWeight, to, both)
        }

        val isDecremental = offeredDistance < oldToDistance
        val isIncremental = offeredDistance > oldToDistance && offeredDistance.parent === oldToDistance.parent
        if (isDecremental) {
            val queue = queue.value ?: run {
                val newQueue = PriorityBlockingQueue<QueuedVertex>(1)
                newQueue.add(QueuedVertex(to, offeredDistance.value))
                queue.compareAndSet(null, newQueue)
                newQueue
            }
            val count = AtomicInteger(0)
            while (queue.isNotEmpty()) {
                val (cur, curDistance) = queue.poll() ?: continue
                for ((neighbor, edge) in cur.outgoing) {
                    count.incrementAndGet()
                    val newDistance = Distance(curDistance + edge.read(), parent = cur)
                    if (neighbor.decrement(newDistance, both)) {
                        queue.add(QueuedVertex(neighbor, newDistance.value))
                    }
                    count.decrementAndGet()
                }
            }
            while (count.get() != 0) {
                Thread.yield()
            }
        } else if (isIncremental) {
            // todo
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