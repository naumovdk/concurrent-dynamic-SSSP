package concurrent.vertex

import Dsssp
import concurrent.process.*
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import java.util.concurrent.ConcurrentHashMap

class Vertex(distance: Double = Dsssp.INF) {
    val outgoing = ConcurrentHashMap<Vertex, Edge>()
    private val descriptor: AtomicRef<Descriptor> = atomic(Descriptor0(Process.UNINITIALIZED))
    private val distance0 = atomic(Distance(distance, null))
    private val distance1 = atomic(Distance.INF)

    private fun readActualDistance(descriptor: Descriptor, status: Status): Distance {
        return when (descriptor) {
            is Descriptor0 -> if (status == Status.SUCCESS) distance0.value else distance1.value
            is Descriptor1 -> if (status == Status.SUCCESS) distance1.value else distance0.value
        }
    }

    private fun readIrrelevantDistance(descriptor: Descriptor, status: Status): Distance {
        return when (descriptor) {
            is Descriptor0 -> if (status == Status.SUCCESS) distance1.value else distance0.value
            is Descriptor1 -> if (status == Status.SUCCESS) distance0.value else distance1.value
        }
    }

    private fun readWorking(descriptor: Descriptor): Distance {
        return when(descriptor) {
            is Descriptor0 -> distance0.value
            is Descriptor1 -> distance1.value
        }
    }

    fun getDistance(): Distance {
        while (true) {
            val curDescriptor = descriptor.value
            val curStatus = curDescriptor.process.status.value
            val curDistance = readActualDistance(curDescriptor, curStatus)
            if (descriptor.value === curDescriptor) {
                return curDistance
            }
        }
    }

    private fun casWorking(descriptor: Descriptor, expect: Distance, update: Distance): Boolean {
        return when (descriptor) {
            is Descriptor0 -> distance0.compareAndSet(expect, update)
            is Descriptor1 -> distance1.compareAndSet(expect, update)
        }
    }

    fun acquire(process: Process): Distance {
        while (true) {
            val curDescriptor = descriptor.value
            val curStatus = curDescriptor.process.status.value
            val curIrrelevant = readIrrelevantDistance(curDescriptor, curStatus)

            val newDescriptor = process.new(curDescriptor, curStatus)

            if (curDescriptor.process === process) {
                return curIrrelevant
            }
            if (curStatus.isInProgress()) {
                process.onIntersection(curDescriptor.process)
                continue
            }
            if (descriptor.compareAndSet(curDescriptor, newDescriptor)) {
                return curIrrelevant
            }
        }
    }

    fun casDistance(process: Process, expect: Distance, newDistance: Distance): Distance {
        val curDescriptor = descriptor.value
        val curStatus = curDescriptor.process.status.value
        val curActual = readActualDistance(curDescriptor, curStatus)

        val update = if (newDistance < curActual) newDistance else curActual

        if (!casWorking(curDescriptor, expect, update)) {
            process.status.compareAndSet(curStatus, Status.ABORTED)
        }
        return update
    }

    fun decrement(offeredDistance: Distance, process: Process): Boolean {
        while (true) {
            val curDescriptor = descriptor.value
            val curStatus = curDescriptor.process.status.value

            if (curDescriptor.process === process) {
                val actual = readActualDistance(curDescriptor, curStatus)
                val expect = readWorking(curDescriptor)
                if (offeredDistance < actual) {
                    if (casWorking(curDescriptor, expect, offeredDistance)) {
                        return true
                    }
                }

                if (casWorking(curDescriptor, expect, actual)) {
                    return false
                }
            }

            if (curStatus.isInProgress()) {
                process.onIntersection(curDescriptor.process)
                continue
            }

            val newDescriptor = process.new(curDescriptor, curStatus)
            descriptor.compareAndSet(curDescriptor, newDescriptor)
        }
    }

    fun plantEdge(expect: Edge?, newWeight: Double, to: Vertex, process: Process): Boolean {
        if (expect == null) {
            val newEdge = Edge(process, Dsssp.INF, newWeight)
            return outgoing.putIfAbsent(to, newEdge) == null
        }

        val update = Edge(process, expect.read(), newWeight)
        return outgoing.replace(to, expect, update)
    }
}