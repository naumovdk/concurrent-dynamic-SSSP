package concurrent.process

import concurrent.process.Status.*
import concurrent.vertex.Distance
import concurrent.vertex.Edge
import concurrent.vertex.QueuedVertex
import concurrent.vertex.Vertex
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import java.util.concurrent.PriorityBlockingQueue

class Process(
    private val priority: Long,
    private val newWeight: Double,
    status: Status = ACQUIRE_FROM
) {
    private val descriptor0 = Descriptor0(this)
    private val descriptor1 = Descriptor1(this)
    val status = atomic(status)

    lateinit var to: Vertex
    lateinit var from: Vertex

    private val fromExpect: AtomicRef<Distance?> = atomic(null)
    private val fromDistance: AtomicRef<Distance?> = atomic(null)

    private val toExpect: AtomicRef<Distance?> = atomic(null)
    private val toDistance: AtomicRef<Distance?> = atomic(null)

    private val offeredDistance: AtomicRef<Distance?> = atomic(null)
    private val edgeExpect: AtomicRef<Edge?> = atomic(null)

    private val queue = atomic<PriorityBlockingQueue<QueuedVertex>?>(null)
    private val top = atomic<QueuedVertex?>(null)

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
        while (true) {
            val curStatus = status.value
            val (cas: Boolean, nextStatus: Status) = when (curStatus) {
                ACQUIRE_FROM -> {
                    val update = from.acquire(this)
                    fromExpect.compareAndSet(null, update)
                    (fromExpect.value!! === update) to UPDATE_FROM_DISTANCE
                }
                UPDATE_FROM_DISTANCE -> {
                    val update = from.casDistance(this, fromExpect.value!!, fromExpect.value!!)
                    fromDistance.compareAndSet(null, update)
                    (fromDistance.value!! === update) to STORE_OFFERED_DISTANCE
                }

                STORE_OFFERED_DISTANCE -> {
                    val update = fromDistance.value!! + newWeight
                    offeredDistance.compareAndSet(null, update)
                    (offeredDistance.value!! === update) to ACQUIRE_TO
                }

                ACQUIRE_TO -> {
                    val update = to.acquire(this)
                    toExpect.compareAndSet(null, update)
                    (toExpect.value!! === update) to UPDATE_TO_DISTANCE
                }
                UPDATE_TO_DISTANCE -> {
                    val update = to.casDistance(this, toExpect.value!!, offeredDistance.value!!)
                    toDistance.compareAndSet(null, update)
                    (toDistance.value!! === update) to READ_EDGE_EXPECT
                }

                READ_EDGE_EXPECT -> {
                    val update = from.outgoing[to]
                    edgeExpect.compareAndSet(null, update)
                    (edgeExpect.value === update) to UPDATE_EDGE
                }
                UPDATE_EDGE -> {
                    from.plantEdge(edgeExpect.value, newWeight, to, this) to SCAN
                }

                SCAN -> {
                    val newQueue = PriorityBlockingQueue<QueuedVertex>(1)
                    newQueue.add(QueuedVertex(to, offeredDistance.value!!.value))
                    queue.compareAndSet(null, newQueue)
                    // inc
                    true to RELAXATION
                }
                RELAXATION -> {
                    val queue = queue.value!!
                    while (true) {
                        val update = queue.peek() ?: break
                        top.compareAndSet(null, update)
                        val readTop = top.value ?: continue
                        val (cur, curDistance) = readTop

                        for ((neighbor, edge) in cur.outgoing) {
                            val w = edge.read()
                            val newDistance = Distance(curDistance + w, parent = cur)
                            if (neighbor.decrement(newDistance, this)) {
                                queue.add(QueuedVertex(neighbor, newDistance.value))
                            }
                        }

                        queue.remove(update)
                        top.compareAndSet(readTop, null)
                    }
                    true to SUCCESS
                }
                SUCCESS -> return
                ABORTED -> return
            }
            val newStatus = if (cas) nextStatus else ABORTED
            status.compareAndSet(curStatus, newStatus)
        }
    }

    fun new(other: Descriptor, status: Status): Descriptor {
        return when (other) {
            is Descriptor0 -> if (status == SUCCESS) descriptor1 else descriptor0
            is Descriptor1 -> if (status == SUCCESS) descriptor0 else descriptor1
        }
    }

    companion object {
        val UNINITIALIZED = Process(0, 0.0, SUCCESS)
    }
}