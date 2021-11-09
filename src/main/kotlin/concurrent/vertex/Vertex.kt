package concurrent.vertex

import Dsssp
import concurrent.process.*
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import java.util.concurrent.ConcurrentHashMap

class Vertex(distance: Double = Dsssp.INF) : Comparable<Vertex> {
    val incoming = ConcurrentHashMap<Vertex, Edge>()
    val outgoing = ConcurrentHashMap<Vertex, Edge>()

    private val descriptor: AtomicRef<Descriptor> = atomic(Descriptor0(Process.UNINITIALIZED))
    private val distance0 = atomic(Distance(distance, null))
    private val distance1 = atomic(Distance.INF)

    data class Improvement(val distance: Distance, val process: Process)
    private val improvement: AtomicRef<Improvement> = atomic(Improvement(Distance.INF, Process.UNINITIALIZED))
    private val decremented = atomic(true to Process.UNINITIALIZED)

    private fun readActual(descriptor: Descriptor, status: Status): Distance {
        return when (descriptor) {
            is Descriptor0 -> if (status == Status.SUCCESS) distance0.value else distance1.value
            is Descriptor1 -> if (status == Status.SUCCESS) distance1.value else distance0.value
        }
    }

    private fun readWorking(descriptor: Descriptor): Distance {
        return when (descriptor) {
            is Descriptor0 -> distance0.value
            is Descriptor1 -> distance1.value
        }
    }

    fun getDistance(): Distance {
        while (true) {
            val curDescriptor = descriptor.value
            val curStatus = curDescriptor.process.status.value
            val curDistance = readActual(curDescriptor, curStatus)
            if (descriptor.value === curDescriptor) {
                return curDistance
            }
        }
    }

    private fun casDistance(expect: Distance, update: Distance): Boolean {
        return when (descriptor.value) {
            is Descriptor0 -> distance0.compareAndSet(expect, update)
            is Descriptor1 -> distance1.compareAndSet(expect, update)
        }
    }

    fun acquire(initiator: Process): Distance? {
        while (true) {
            val curDescriptor = descriptor.value
            val curStatus = curDescriptor.process.status.value
            val curActual = readActual(curDescriptor, curStatus)

            if (initiator.status.value.isTerminated()) {
                return null
            }

            if (curDescriptor.process === initiator) {
                return curActual
            }

            if (curStatus.isInProgress()) {
                initiator.onIntersection(curDescriptor.process)
                continue
            }

            val newDescriptor = initiator.new(curDescriptor, curStatus)
            descriptor.compareAndSet(curDescriptor, newDescriptor)
        }
    }

    fun tryDecrement(newDistance: Distance, initiator: Process): Pair<Boolean, Distance>? {
        while (true) {
            val curDescriptor = descriptor.value
            val curStatus = curDescriptor.process.status.value
            val curActual = readActual(curDescriptor, curStatus)
            val curWorking = readWorking(curDescriptor)
            val curImprovement = improvement.value
            val curDecremented = decremented.value

            if (initiator.status.value.isTerminated()) {
                return null
            }

            if (curImprovement.process !== initiator) {
                improvement.compareAndSet(curImprovement, Improvement(curActual, initiator))
                continue
            }

            if (newDistance < curImprovement.distance) {
                decremented.compareAndSet(curDecremented, true to initiator)
                improvement.compareAndSet(curImprovement, Improvement(newDistance, initiator))
                continue
            }

            val copy = curImprovement.distance.copy()
            casDistance(curWorking, copy)
            if (readWorking(curDescriptor) == copy) {
                return (curDecremented.first && curDecremented.second === initiator) to copy
            }
        }
    }

    fun setEdge(
        edges: ConcurrentHashMap<Vertex, Edge>,
        expect: Edge?,
        newWeight: Double,
        to: Vertex,
        process: Process
    ): Boolean {
        if (expect == null) {
            val newEdge = Edge(process, Dsssp.INF, newWeight)
            return edges.putIfAbsent(to, newEdge) == null
        }

        val update = Edge(process, expect.read(process), newWeight)
        return edges.replace(to, expect, update)
    }

    fun acquireIfChild(
        parent: Vertex,
        initiator: Process
    ): Boolean? {
        while (true) {
            val curDescriptor = descriptor.value
            val curStatus = curDescriptor.process.status.value
            val curActual = readActual(curDescriptor, curStatus)
            val curImprovement = improvement.value

            if (initiator.status.value.isTerminated()) {
                return null
            }

            if (curDescriptor.process === initiator) {
                if (curActual.parent === parent) {
                    improvement.compareAndSet(curImprovement, Improvement(Distance.INF, initiator))
                    return true
                }
                return false
            }

            if (curStatus.isInProgress()) {
                initiator.onIntersection(curDescriptor.process)
                continue
            }

            val newDescriptor = initiator.new(curDescriptor, curStatus)
            descriptor.compareAndSet(curDescriptor, newDescriptor)
        }
    }

    override fun compareTo(other: Vertex): Int {
        return compareValuesBy(this, other) { it.hashCode() }
    }

    fun mark(process: Process) {
        val dec = decremented.value
        if (descriptor.value.process === process) {
            decremented.compareAndSet(dec, false to process)
        }
    }
}