package concurrent.process

import Dsssp
import Dsssp.Companion.supportDecremental
import Dsssp.Companion.supportIncremental
import concurrent.process.Status.*
import concurrent.vertex.Distance
import concurrent.vertex.QueuedVertex
import concurrent.vertex.Vertex
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.PriorityBlockingQueue

class Process(private val priority: Long, private val newWeight: Double, status: Status = INIT) {
    private val descriptor0 = Descriptor0(this)
    private val descriptor1 = Descriptor1(this)
    val status = atomic(status)

    lateinit var to: Vertex
    lateinit var from: Vertex
    lateinit var source: Vertex

    private val priorityQueue = atomic<PriorityBlockingQueue<QueuedVertex>?>(null)
    private val top = atomic<QueuedVertex?>(null)

    @Volatile
    private var incremental = false

    private val workSet: AtomicRef<ConcurrentSkipListSet<Vertex>?> = atomic(null)
    private val affected: AtomicRef<ConcurrentSkipListSet<Vertex>?> = atomic(null)
    private val topWorkSet: AtomicRef<Vertex?> = atomic(null)

    fun onIntersection(other: Process) {
        if (Dsssp.supportHelp && this.priority < other.priority) {
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
            when (curStatus) {
                INIT -> {
                    val fromDistance = from.acquire(this) ?: return
                    from.tryDecrement(fromDistance, this) ?: return

                    val toCurDistance = to.acquire(this) ?: return
                    val offered = Distance(fromDistance.distance + newWeight, parent = from)
                    val update = if (offered < toCurDistance) offered else toCurDistance

                    if (supportIncremental && toCurDistance.parent === from && offered > toCurDistance) {
                        incremental = true
                        to.acquireIfChild(from, this)
                    } else {
                        val (_, toDistance) = to.tryDecrement(update, this) ?: return

                        priorityQueue.compareAndSet(
                            null,
                            PriorityBlockingQueue(listOf(QueuedVertex(to, toDistance)))
                        )
                    }

                    from.setEdge(from.outgoing, from.outgoing[to], newWeight, to, this)
                    to.setEdge(to.incoming, to.incoming[from], newWeight, from, this)
                }

                SCAN -> {
                    if (supportIncremental && incremental) {
                        workSet.compareAndSet(null, ConcurrentSkipListSet(listOf(to)))
                        affected.compareAndSet(null, ConcurrentSkipListSet())

                        val workSet = workSet.value!!
                        val affected = affected.value!!

                        while (workSet.isNotEmpty()) {
                            val first = try {
                                workSet.first()
                            } catch (e: NoSuchElementException) {
                                break
                            }

                            topWorkSet.compareAndSet(null, first)
                            val cur = topWorkSet.value ?: continue

                            affected.add(cur)

                            for (neighbor in cur.outgoing.keys()) {
                                if (!affected.contains(neighbor) && neighbor !== from) { // mb delete from check
                                    if (neighbor.acquireIfChild(cur, this) ?: continue) {
                                        workSet.add(neighbor)
                                    }
                                }
                            }

                            topWorkSet.compareAndSet(first, null)
                            workSet.remove(first)
                        }

                        val starting = mutableSetOf<QueuedVertex>()
                        for (a in affected) {
                            for (parent in a.incoming.keys()) {
                                if (!affected.contains(parent)) {
                                    val actual = parent.acquire(this) ?: return
                                    parent.tryDecrement(actual, this)
                                    starting.add(QueuedVertex(parent, actual))
                                }
                            }
                        }

                        val q = PriorityBlockingQueue(starting)
                        priorityQueue.compareAndSet(null, q)
                    }
                }

                RELAXATION -> {
                    val q = priorityQueue.value!!
                    while (supportDecremental) {
                        val update = q.peek() ?: break

                        top.compareAndSet(null, update)
                        val readTop = top.value ?: continue
                        val (cur, curDistance) = readTop

                        for ((neighbor, edge) in cur.outgoing) {
                            val actual = neighbor.acquire(this) ?: return

                            val w = edge.read(this)
                            val offered = Distance(curDistance.distance + w, parent = cur)
                            val newDistance =
                                if ((supportIncremental && incremental && affected.value!!.contains(neighbor))
                                    || offered < actual
                                ) offered else actual

                            val (decremented, real) = neighbor.tryDecrement(newDistance, this) ?: return
                            if (decremented) {
                                q.add(QueuedVertex(neighbor, real))
                                neighbor.mark(this)
                            }
                        }

                        top.compareAndSet(readTop, null)
                        q.remove(readTop)
                    }
                }

                UPDATE_DISTANCES -> {

                }
                SUCCESS -> return
                ABORTED -> return
            }

            status.compareAndSet(curStatus, curStatus.next())
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