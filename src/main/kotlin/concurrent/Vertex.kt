package concurrent

import Dsssp
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

class Vertex(distance: Double = Dsssp.INF) {
    private val outgoing = ConcurrentHashMap<Vertex, Edge>()
    private val descriptor: AtomicReference<Descriptor> = AtomicReference(Descriptor0(Process.UNINITIALIZED))
    private val distance0 = AtomicReference(Distance(distance, null))
    private val distance1 = AtomicReference(Distance.INF)

    private fun actual(descriptor: Descriptor, status: Status): AtomicReference<Distance> {
        return when (descriptor) {
            is Descriptor0 -> if (status == Status.SUCCESS) distance0 else distance1
            is Descriptor1 -> if (status == Status.SUCCESS) distance1 else distance0
        }
    }

    private fun other(descriptor: Descriptor, status: Status) =
        if (actual(descriptor, status) === distance0) distance1 else distance0

    fun getDistance(): Distance {
        while (true) {
            val curDescriptor = descriptor.get()
            val curStatus = curDescriptor.process.status.value
            val fixed = if (curStatus.isNotInProgress()) {
                actual(curDescriptor, curStatus)
            } else {
                other(curDescriptor, curStatus)
            }.get()
            if (descriptor.get() === curDescriptor) {
                return fixed
            }
        }
    }

    fun acquire(newDistance: Distance?, both: BothDescriptors): Distance { // поменяет расстояние, даже если увеличивается
        while (true) {
            val curDescriptor = descriptor.get()
            val curStatus = curDescriptor.process.status.value
            val newDescriptor = both.new(curDescriptor)

            if (curDescriptor === newDescriptor) {
                return actual(curDescriptor, curStatus).get()
            }
            if (curStatus.isInProgress()) {
                curDescriptor.process.onIntersection(both.process)
                continue
            }
            if (descriptor.compareAndSet(curDescriptor, newDescriptor)) {
                val curDistance = actual(curDescriptor, curStatus).get()
                return other(curDescriptor, curStatus).getAndSet(newDistance ?: return curDistance)
            }
        }
    }

    fun decrement(newDistance: Distance, both: BothDescriptors): Boolean {
        while (true) {
            val curDescriptor = descriptor.get()
            val curStatus = curDescriptor.process.status.value
            val newDescriptor = both.new(curDescriptor)

            if (curDescriptor === newDescriptor) {
                return false
            }
            if (curStatus.isInProgress()) {
                curDescriptor.process.onIntersection(both.process)
                continue
            }
            val curDistance = actual(curDescriptor, curStatus).get()
            if (curDistance <= newDistance) {
                return false
            }
            if (descriptor.compareAndSet(curDescriptor, newDescriptor)) {
                other(curDescriptor, curStatus).getAndSet(newDistance)
                return true
            }
        }
    }

    fun plantEdge(newWeight: Double, to: Vertex) {
        TODO("Not yet implemented")
    }
}