package concurrent.process

import Dsssp
import concurrent.process.Status.*
import concurrent.vertex.Distance
import concurrent.vertex.QueuedVertex
import concurrent.vertex.Vertex
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.PriorityBlockingQueue

class Process(private val priority: Long, private val newWeight: Double, status: Status = ACQUIRE_FROM) {
    private val descriptor0 = Descriptor0(this)
    private val descriptor1 = Descriptor1(this)
    val status = atomic(status)

    lateinit var to: Vertex
    lateinit var from: Vertex

    private val casMap = ConcurrentHashMap<Vertex, Pair<Distance, Distance>>()

    private val priorityQueue = atomic<PriorityBlockingQueue<QueuedVertex>?>(null)
    private val top = atomic<QueuedVertex?>(null)

    private val isIncremental = atomic(false)

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
                ACQUIRE_FROM -> {
                    val (actual, expect) = from.acquire(this) ?: return
                    casMap.putIfAbsent(from, expect to actual)
                }
                ACQUIRE_TO -> {
                    val (actual, expect) = to.acquire(this) ?: return
                    val offered = Distance(casMap[from]!!.second.value + newWeight, parent = from)
                    if (actual.parent === from && offered > actual) {
                        isIncremental.getAndSet(true)
                    }
                    val update = if (offered < actual) {
                        offered
                    } else {
                        actual
                    }
                    if (Dsssp.supportInc && actual.parent === from && offered > actual) {
                        isIncremental.getAndSet(true)
                    } else {
                        casMap.putIfAbsent(to, expect to update)
                    }
                }

                UPDATE_OUTGOING -> from.plantEdge(from.outgoing, from.outgoing[to], newWeight, to, this)

                UPDATE_INCOMING -> to.plantEdge(to.incoming, to.incoming[from], newWeight, from, this)

                SCAN -> {
                    if (isIncremental.value && Dsssp.supportInc) {
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
                                if (!affected.contains(neighbor) && neighbor !== from) {
                                    val expect = neighbor.acquireIfChild(cur, this) ?: continue
                                    casMap.putIfAbsent(neighbor, expect to Distance.INF)
                                    workSet.add(neighbor)
                                }
                            }

                            topWorkSet.compareAndSet(first, null)
                            workSet.remove(first)
                        }

                        val starting = mutableSetOf<QueuedVertex>()
                        for (a in affected) {
                            for (parent in a.incoming.keys()) {
                                if (!affected.contains(parent)) {
                                    val (actual, expect) = parent.acquire(this) ?: return
                                    starting.add(QueuedVertex(parent, actual))
                                    casMap.putIfAbsent(parent, expect to actual)
                                }
                            }
                        }

                        priorityQueue.compareAndSet(null, PriorityBlockingQueue(starting))
                    }
                }

                RELAXATION -> {
                    val q = priorityQueue.value ?: run {
                        priorityQueue.compareAndSet(
                            null,
                            PriorityBlockingQueue(listOf(QueuedVertex(to, casMap[to]!!.second)))
                        )
                        priorityQueue.value!!
                    }
                    while (Dsssp.supportDec) {
                        val update = q.peek() ?: break

                        top.compareAndSet(null, update)
                        val readTop = top.value ?: continue

                        val (cur, curDistance) = readTop

                        for ((neighbor, edge) in cur.outgoing) {
                            val (actual, expect) = neighbor.acquire(this) ?: return

                            val w = edge.read(this)
                            val offered = Distance(curDistance.value + w, parent = cur)
                            val newDistance =
                                if (Dsssp.supportInc && isIncremental.value && affected.value!!.contains(neighbor) || offered < actual)
                                    offered else actual

                            casMap.compute(neighbor) { _, mapped ->
                                if (mapped == null || newDistance < mapped.second) {
                                    q.add(QueuedVertex(neighbor, newDistance))
                                    expect to newDistance
                                } else {
                                    mapped
                                }
                            }
                        }

                        top.compareAndSet(readTop, null)
                        q.remove(update)
                    }
                }

                UPDATE_DISTANCES -> {
                    for ((vertex, distances) in casMap) {
                        val (expect, actual) = distances
                        if (!vertex.casDistance(expect, actual.copy())) return
                    }
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