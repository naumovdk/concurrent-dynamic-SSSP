package concurrent

import concurrent.Status.*
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class GlobalDescriptor(
    private val priority: Long,
    private val priorityQueue: AtomicReference<PriorityBlockingQueue<Vertex>?> = AtomicReference(null),
    val status: AtomicReference<Status> = AtomicReference(IN_PROGRESS),
) {
    lateinit var from: Vertex
    lateinit var to: Vertex
    var newWeight = 0.0

    fun helpOrTurnOff(other: GlobalDescriptor) {
        if (this.priority < other.priority) {
            other.help()
        } else {
            other.status.compareAndSet(IN_PROGRESS, ABORTED)
        }
    }

    fun help() {
        val fromLocal = from.local.get()
        if (fromLocal.global !== this) {
            return
        }
        val offeredDistance = Distance(fromLocal.newDistance.value + newWeight, from)

        val oldDistance = to.acquire(offeredDistance, this) ?: return

        val priorityQueue = priorityQueue.get() ?: run {
            val newQueue: PriorityBlockingQueue<Vertex> = PriorityBlockingQueue(1)
            newQueue.add(to)
            priorityQueue.compareAndSet(null, newQueue)
            priorityQueue.get()!!
        }

        if (offeredDistance < oldDistance) {
            val count = AtomicInteger(0)
            while (priorityQueue.isNotEmpty()) {
                val cur = priorityQueue.poll() ?: continue
                val curDist = cur.local.get().newDistance
                cur.outgoing.forEach { (neighbor, edge) ->
                    count.incrementAndGet()
                    val newDist = Distance(curDist.value + edge.readWeight(), cur)
                    if (neighbor.acquireIfImproves(newDist, this) ?: run { return }) {
                        priorityQueue.add(neighbor)
                    }
                    count.decrementAndGet()
                }
            }
            while (count.get() != 0) {
                Thread.yield()
            }
        } else if (offeredDistance > oldDistance) {
            if (oldDistance.parent === from) {
                // todo incremental
            }
        }
        status.compareAndSet(IN_PROGRESS, SUCCESS)
    }

    companion object {
        val FAKE = GlobalDescriptor(-1, status = AtomicReference(SUCCESS))
    }
}