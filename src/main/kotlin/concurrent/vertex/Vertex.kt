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

    fun getDistance(): Distance {
        while (true) {
            val curDescriptor = descriptor.value
            val curStatus = curDescriptor.process.status.value
            val curDistance = readDistance(curDescriptor, curStatus)
            if (descriptor.value === curDescriptor) {
                return curDistance
            }
        }
    }

    private fun readDistance(descriptor: Descriptor, status: Status): Distance {
        return when (descriptor) {
            is Descriptor0 -> if (status == Status.SUCCESS) distance0.value else distance1.value
            is Descriptor1 -> if (status == Status.SUCCESS) distance1.value else distance0.value
        }
    }

    fun acquire(distance: Distance?, both: BothDescriptors, expected: AtomicRef<Distance?>): Distance {
        while (true) {
            val curDescriptor = descriptor.value
            val curStatus = curDescriptor.process.status.value

            val curDistance = readDistance(curDescriptor, curStatus)


            val newDistance = when {
                distance == null -> curDistance
                distance >= curDistance -> curDistance
                else -> distance
            }

            val newDescriptor = both.new(curDescriptor, curStatus)

            if (expected.value == null) {
                when (newDescriptor) {
                    is Descriptor0 -> expected.compareAndSet(null, distance0.value)
                    is Descriptor1 -> expected.compareAndSet(null, distance1.value)
                }
            }

            if (curDescriptor.process === both.process) {
                when (curDescriptor) {
                    is Descriptor0 -> distance0.compareAndSet(expected.value!!, newDistance)
                    is Descriptor1 -> distance1.compareAndSet(expected.value!!, newDistance)
                }

                return when (curDescriptor) {
                    is Descriptor0 -> distance0.value
                    is Descriptor1 -> distance1.value
                }
            }
            if (curStatus.isInProgress()) {
                both.process.onIntersection(curDescriptor.process)
                continue
            }

            if (descriptor.compareAndSet(curDescriptor, newDescriptor)) {
                if (when (newDescriptor) {
                        is Descriptor0 -> {
                            distance0.compareAndSet(curDistance, newDistance)
                        }
                        is Descriptor1 -> distance1.compareAndSet(curDistance, newDistance)
                    }
                ) {
                    return curDistance
                }
            }
        }
    }

    fun decrement(offeredDistance: Distance, both: BothDescriptors): Boolean {
        while (true) {
            val curDescriptor = descriptor.value
            val curStatus = curDescriptor.process.status.value

            val isNotInversion = curDescriptor.process !== both.process
            val curDistance = if (isNotInversion) {
                if (curStatus.isInProgress()) {
                    both.process.onIntersection(curDescriptor.process)
                    continue
                }
                when (curDescriptor) {
                    is Descriptor0 -> if (curStatus == Status.SUCCESS) distance0.value else distance1.value
                    is Descriptor1 -> if (curStatus == Status.SUCCESS) distance1.value else distance0.value
                }
            } else {
                when (curDescriptor) {
                    is Descriptor0 -> distance0.value
                    is Descriptor1 -> distance1.value
                }
            }

            if (curDistance <= offeredDistance) {
                return false
            }

            if (isNotInversion) {
                val newDescriptor = both.new(curDescriptor, curStatus)
                if (descriptor.compareAndSet(curDescriptor, newDescriptor)) {
                    when (newDescriptor) {
                        is Descriptor0 -> distance0.getAndSet(offeredDistance)
                        is Descriptor1 -> distance1.getAndSet(offeredDistance)
                    }
                }
            } else {
                when (curDescriptor) {
                    is Descriptor0 -> distance0.getAndSet(offeredDistance)
                    is Descriptor1 -> distance1.getAndSet(offeredDistance)
                }
            }
            return true
        }
    }

    fun plantEdge(newWeight: Double, to: Vertex, both: BothDescriptors) {
        val newEdge = Edge(newWeight)
        val mapped = outgoing.getOrPut(to) { newEdge }
        if (mapped === newEdge) {
            return
        }
        mapped.set(newWeight, both)
    }
}