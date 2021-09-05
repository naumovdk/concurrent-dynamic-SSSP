package concurrent

import concurrent.Status.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.atomic.AtomicReference

class GlobalDescriptor(
    val status: AtomicReference<Status>,
    private val vertexes: ConcurrentHashMap<Int, Vertex>,
    private val priority: Long,
    private val priorityQueue: AtomicReference<PriorityBlockingQueue<Vertex>?> = AtomicReference(null),
    private val offeredDistance: Distance,
    private val from: Vertex,
    private val to: Vertex
) {
    fun helpOrTurnOff(other: GlobalDescriptor) {
        if (this.priority <= other.priority) {
            other.help()
        } else {
            this.status.compareAndSet(IN_PROGRESS, ABORTED)
        }
    }

    fun help() {
        val oldDistance = if (to.local.get().global !== this) {
            to.acquire(offeredDistance, this)
        } else {
            to.local.get().oldDistance
        }
        val priorityQueue = priorityQueue.get() ?: run {
            priorityQueue.compareAndSet(null, PriorityBlockingQueue(1))
            priorityQueue.get()!!
        }
        when {
            offeredDistance < oldDistance -> {
                while (priorityQueue.isNotEmpty()) {
                    val cur = priorityQueue.poll() ?: continue
                    val curDist = cur.local.get().newDistance
                    cur.outgoing.forEach { (i, w) ->
                        val neighbor = vertexes[i]!!
                        val newDist = Distance(curDist.value + w, cur)
                        if (neighbor.acquireIfImproves(newDist, this) ?: run { return }) {
                            priorityQueue.add(neighbor)
                        }
                    }
                }
            }
            oldDistance < offeredDistance -> {
                if (oldDistance.parent === from) {
                    status.set(ABORTED)
                    // do nothing!
                    // no edge being set
                    return
                }
            }
        }
        status.compareAndSet(IN_PROGRESS, SUCCESS)
    }
}