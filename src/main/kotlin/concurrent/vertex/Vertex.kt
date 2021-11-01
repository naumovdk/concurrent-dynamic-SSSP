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

    private fun readActual(descriptor: Descriptor, status: Status): Distance {
        return when (descriptor) {
            is Descriptor0 -> if (status == Status.SUCCESS) distance0.value else distance1.value
            is Descriptor1 -> if (status == Status.SUCCESS) distance1.value else distance0.value
        }
    }

    fun readWorking(descriptor: Descriptor): Distance {
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

    /**
     * @return pair of actual and expect distances or null if initiator has been aborted
     */
    fun acquire(initiator: Process): Pair<Distance, Distance>? {
        while (true) {
            val curDescriptor = descriptor.value
            val curStatus = curDescriptor.process.status.value
            val curActual = readActual(curDescriptor, curStatus)

            if (curDescriptor.process === initiator) {
                if (curStatus.isNotInProgress()) {
                    return null
                }
                return curActual to readWorking(curDescriptor)
            }

            if (curStatus.isInProgress()) {
                initiator.onIntersection(curDescriptor.process)
                continue
            }

            val newDescriptor = initiator.new(curDescriptor, curStatus)
            descriptor.compareAndSet(curDescriptor, newDescriptor)
        }
    }

    fun acquire2(initiator: Process): Pair<Distance, Distance>? {
        while (true) {
            val curDescriptor = descriptor.value
            val curStatus = curDescriptor.process.status.value
            val curActual = readActual(curDescriptor, curStatus)

            if (curDescriptor.process === initiator) {
                if (curStatus.isNotInProgress()) {
                    return null
                }
                return readWorking(curDescriptor) to readWorking(curDescriptor)
            }

            if (curStatus.isInProgress()) {
                initiator.onIntersection(curDescriptor.process)
                continue
            }

            val newDescriptor = initiator.new(curDescriptor, curStatus)
            if (descriptor.compareAndSet(curDescriptor, newDescriptor)) {
                return curActual to readWorking(newDescriptor)
            }
        }
    }

    fun acquireAndCas(initiator: Process, update: Distance): Boolean? {
        while (true) {
            val curDescriptor = descriptor.value
            val curStatus = curDescriptor.process.status.value
            val curActual = readActual(curDescriptor, curStatus)

            if (curDescriptor.process === initiator) {
                if (curStatus.isNotInProgress()) {
                    return null
                }
                val expect = readWorking(curDescriptor)
                if (update > curActual) {
                    if (casDistance(expect, curActual)) {
                        return false
                    }
                } else {
                    if (casDistance(expect, update)) {
                        return true
                    }
                }
            }

            if (curStatus.isInProgress()) {
                initiator.onIntersection(curDescriptor.process)
                continue
            }

            val newDescriptor = initiator.new(curDescriptor, curStatus)
            descriptor.compareAndSet(curDescriptor, newDescriptor)
        }
    }

    fun casDistance(expect: Distance, update: Distance): Boolean {
        return when (descriptor.value) {
            is Descriptor0 -> distance0.compareAndSet(expect, update)
            is Descriptor1 -> distance1.compareAndSet(expect, update)
        }
    }

    fun plantEdge(
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

    /**
     * @return expect distance or null if initiator has been aborted or parents are not equal
     */
    fun acquireIfChild(
        parent: Vertex,
        initiator: Process
    ): Distance? {
        while (true) {
            val curDescriptor = descriptor.value
            val curStatus = curDescriptor.process.status.value
            val curActual = readActual(curDescriptor, curStatus)

            if (curDescriptor.process === initiator) {
                if (curStatus.isNotInProgress()) {
                    return null
                }
                return readWorking(curDescriptor)
            }

            if (curStatus.isInProgress()) {
                initiator.onIntersection(curDescriptor.process)
                continue
            }

            val newDescriptor = initiator.new(curDescriptor, curStatus)
            if (curActual.parent === parent) {
                descriptor.compareAndSet(curDescriptor, newDescriptor)
            } else {
                return null
            }
        }
    }

    override fun compareTo(other: Vertex): Int {
        return compareValuesBy(this, other) { it.hashCode() }
    }
}