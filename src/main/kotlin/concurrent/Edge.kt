package concurrent

import Dsssp
import concurrent.Status.SUCCESS
import java.util.concurrent.atomic.AtomicReference


class Edge(weight: Double) {
    private val descriptor: AtomicReference<Descriptor> = AtomicReference(Descriptor0(Process.UNINITIALIZED))
    private val edge0 = AtomicReference(weight)
    private val edge1 = AtomicReference(Dsssp.INF)

    private fun actual(descriptor: Descriptor, status: Status): AtomicReference<Double> {
        assert(status.isNotInProgress())
        return when (descriptor) {
            is Descriptor0 -> if (status == SUCCESS) edge0 else edge1
            is Descriptor1 -> if (status == SUCCESS) edge1 else edge0
        }
    }

    private fun other(descriptor: Descriptor, status: Status) =
        if (actual(descriptor, status) == edge0) edge1 else edge0

    fun read(): Double {
        val curDescriptor = descriptor.get()
        val curStatus = curDescriptor.process.status.value
        return actual(curDescriptor, curStatus).get()
    }

    fun set(newWeight: Double, both: BothDescriptors) {
        val curDescriptor = descriptor.get()
        val curStatus = curDescriptor.process.status.value
        val curWeight = actual(curDescriptor, curStatus).get()
        if (curDescriptor.process === both.process && curWeight != newWeight && curStatus.isInProgress()) { // плохо
            other(curDescriptor, curStatus).compareAndSet(curWeight, newWeight)
        } else {
            val newDescriptor = both.new(curDescriptor)
            // если кас провалился то:
            // --- либо кто-то параллельно с нами поставил это же ребро
            // --- либо процесс абортировали и дескриптор уже поменялся, ребро уже не надо ставить
            if (descriptor.compareAndSet(curDescriptor, newDescriptor)) {
                // может пройти кас, но мы не поставили вес
                // другой поток поставит SUCCESS, но ребра нет
                other(curDescriptor, curStatus).compareAndSet(curWeight, newWeight)
            }
        }
        // поэтому ставим бул, чтобы все потоки знали, поставили ли ребро
        both.process.successfullySetEdge.compareAndSet(false, update = true)
        return
    }
}