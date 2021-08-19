import DssspImpl.Status.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.Double.Companion.POSITIVE_INFINITY

class DssspImpl(source: Int) : Dsssp() {
    private val vertexes = ConcurrentHashMap<Int, Vertex>()

    class Descriptor(
        val queue: PriorityQueue<Pair<Double, Vertex>>,
        val status: AtomicReference<Status>,
        private val operation: () -> Boolean
    ) {
        fun abort(): Boolean {
            status.set(ABORTED)
            return operation.invoke()
        }
    }

    class Distance(var oldDistance: Double = POSITIVE_INFINITY, var newDistance: Double = POSITIVE_INFINITY) {
        fun read(status: Status) : Double {
            assert(status != IN_PROGRESS)
            return if (status != ABORTED) {
                newDistance
            } else {
                oldDistance
            }
        }
    }

    enum class Status {
        SUCCESS, IN_PROGRESS, ABORTED
    }

    class Vertex(val index: Int) {
        val outgoing = ConcurrentHashMap<Int, Double>()
        var distance = AtomicReference(Distance())
        private val defaultDescriptor = Descriptor(PriorityQueue(), AtomicReference(SUCCESS)) { true }
        private var descriptor: AtomicReference<Descriptor> = AtomicReference(defaultDescriptor)

        fun readDistance(): Double {
            while (true) {
                val distance = distance.get()
                val status = descriptor.get().status.get()
                if (status != IN_PROGRESS) {
                    return distance.read(status)
                }
            }
        }

        fun acquireAndGet(newDescriptor: Descriptor): Pair<Double, Status> {
            while (true) {
                val curDescriptor = descriptor.get()
                val curStatus = curDescriptor.status.get()
                if (curStatus != IN_PROGRESS) {
                    if (descriptor.compareAndSet(curDescriptor, newDescriptor)) {
                        return distance.get().read(curStatus) to curStatus
                    }
                } else {
                    curDescriptor.abort()
                }
            }
        }

        fun release() {
            assert(descriptor.get().status.get() == IN_PROGRESS)
            descriptor.set(defaultDescriptor)
        }
    }

    init {
        vertexes[source] = Vertex(source)
        vertexes[source]!!.distance.set(Distance(newDistance = 0.0, oldDistance = POSITIVE_INFINITY))
    }

    override fun getDistance(index: Int): Double? {
        val vertex = vertexes[index] ?: return null
        return vertex.readDistance()
    }

    override fun setEdge(fromIndex: Int, toIndex: Int, newWeight: Double): Boolean {
        if (fromIndex == toIndex) {
            return false
        }
        val from = vertexes[fromIndex] ?: return false
        val to = vertexes[toIndex] ?: return false

        val newDescriptor = Descriptor(
            PriorityQueue(compareBy { it.first }),
            AtomicReference(IN_PROGRESS)
        ) { setEdge(fromIndex, toIndex, newWeight) }

        val fromDistance = from.readDistance()
        val (toDistance, toStatus) = to.acquireAndGet(newDescriptor)
        val newDistance = fromDistance + newWeight
        return if (newDistance < toDistance) {
            from.outgoing[toIndex] = newWeight
            to.distance.set(Distance(oldDistance = toDistance, newDistance = newDistance))
            newDescriptor.queue.add(newDistance to to)
            decremental(newDescriptor)
        } else {
            newDescriptor.status.set(toStatus)
            false // only decremental for now
        }
    }

    private fun decremental(newDescriptor: Descriptor): Boolean {
        while (newDescriptor.queue.isNotEmpty()) {
            val (curDistance, cur) = newDescriptor.queue.poll()
            for ((index, w) in cur.outgoing) {
                val neighbour = vertexes[index]!!
                val (oldDistance, _) = neighbour.acquireAndGet(newDescriptor)
                val newDistance = curDistance + w
                if (newDistance < oldDistance) {
                    neighbour.distance.set(Distance(oldDistance = oldDistance, newDistance = newDistance))
                    newDescriptor.queue.add(newDistance to neighbour)
                } else {
                    neighbour.release()
                }
            }
        }
        newDescriptor.status.set(SUCCESS)
        return true
    }

    override fun addVertex(index: Int): Boolean {
        val newVertex = Vertex(index)
        val mapped = vertexes.getOrPut(index) { newVertex }
        return mapped === newVertex
    }

    override fun removeEdge(fromIndex: Int, toIndex: Int): Boolean {
        TODO()
    }

    override fun removeVertex(index: Int): Boolean {
        TODO()
    }
}