package concurrent.vertex

import Dsssp
import concurrent.process.*
import kotlinx.atomicfu.atomic


class Edge(weight: Double) {
    class WrappedDouble(val value: Double)

    private val descriptor = atomic(Descriptor.UNINITIALIZED)
    private val edge0 = atomic(WrappedDouble(weight))
    private val edge1 = atomic(WrappedDouble(Dsssp.INF))

    fun read(): Double {
        while (true) {
            val curDescriptor = descriptor.value
            val curStatus = curDescriptor.process.status.value
            val res = when (curDescriptor) {
                is Descriptor0 -> if (curStatus == Status.SUCCESS) edge0.value else edge1.value
                is Descriptor1 -> if (curStatus == Status.SUCCESS) edge1.value else edge0.value
            }.value
            if (descriptor.value === curDescriptor) {
                return res
            }
        }
    }

    fun set(weight: Double, both: BothDescriptors) {
        val newWeight = WrappedDouble(weight)
        val curDescriptor = descriptor.value
        val curStatus = curDescriptor.process.status.value

        if (curDescriptor.process !== both.process) {
            assert(curStatus.isFinished())

            val newDescriptor = both.new(curDescriptor, curStatus)
            descriptor.compareAndSet(curDescriptor, newDescriptor)
            when (newDescriptor) {
                is Descriptor0 -> edge0.getAndSet(newWeight)
                is Descriptor1 -> edge1.getAndSet(newWeight)
            }
        }

        val curIrrelevant = when (curDescriptor) {
            is Descriptor0 -> edge0.value
            is Descriptor1 -> edge1.value
        }

        if (descriptor.value === curDescriptor) { // still needs help
            when (curDescriptor) {
                is Descriptor0 -> edge0.compareAndSet(curIrrelevant, newWeight)
                is Descriptor1 -> edge1.compareAndSet(curIrrelevant, newWeight)
            }
        }

        both.process.successfullySetEdge.compareAndSet(false, update = true)
        return
    }
}