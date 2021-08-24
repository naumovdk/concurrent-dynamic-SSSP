import DssspImpl.Status.*
import java.lang.Exception
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.Double.Companion.POSITIVE_INFINITY

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class DssspImpl(source: Int) : Dsssp() {
    private val vertexes = ConcurrentHashMap<Int, Vertex>()

    class Descriptor(
        var queue: PriorityQueue<Pair<Double, Vertex>>,
        val status: AtomicReference<Status>,
    )

    class Distance(
        val oldDistance: Double = POSITIVE_INFINITY,
        val newDistance: Double = POSITIVE_INFINITY,
    ) {
        fun read(status: Status, changed: Boolean): Double {
            return when (status) {
                SUCCESS -> newDistance
                IN_PROGRESS -> throw Exception("read vertex in progress")
                ABORTED -> if (changed) oldDistance else newDistance
            }
        }
    }

    enum class Status {
        SUCCESS, IN_PROGRESS, ABORTED
    }

    class Vertex {
        val outgoing = ConcurrentHashMap<Int, Double>()
        var distance = AtomicReference(Distance())
        private val defaultDescriptor = Descriptor(PriorityQueue(), AtomicReference(SUCCESS))
        private val readDescriptor = Descriptor(PriorityQueue(), AtomicReference(IN_PROGRESS))
        val descriptor: AtomicReference<Descriptor> = AtomicReference(defaultDescriptor)
        val changed = AtomicBoolean(true)

        fun readDistance(): Double {
            while (true) {
                val curDescriptor = descriptor.get()
                val curStatus = curDescriptor.status.get()
                if (curStatus != IN_PROGRESS) {
                    if (descriptor.compareAndSet(curDescriptor, readDescriptor)) {
                        val distance = distance.get()
                        val changed = changed.get()
                        val res = distance.read(curStatus, changed)
                        descriptor.set(curDescriptor)
                        return res
                    }
                }
            }
        }

        fun acquireAndGet(newDescriptor: Descriptor): Double? {
            while (true) {
                val curDescriptor = descriptor.get()
                val curStatus = curDescriptor.status.get()
                val curChanged = changed.get()
                if (curStatus != IN_PROGRESS) {
                    if (descriptor.compareAndSet(curDescriptor, newDescriptor)) {
                        val curDistance = distance.get()
                        val actualDistance = curDistance.read(curStatus, curChanged)
                        assert(distance.compareAndSet(curDistance, Distance(newDistance = actualDistance, oldDistance = 1.234567)))
                        changed.set(false)
                        return actualDistance
                    }
                } else {
                    return null
                }
            }
        }

        fun release() {
            assert(descriptor.get().status.get() == IN_PROGRESS)
            descriptor.set(defaultDescriptor)
        }
    }

    init {
        val root = Vertex()
        root.distance.set(Distance(newDistance = 0.0, oldDistance = POSITIVE_INFINITY))
        vertexes[source] = root

        for (i in 0..INITIAL_GRAPH_SIZE) {
            addVertex(i)
        }
    }

    override fun getDistance(index: Int): Double? {
        return vertexes[index]?.readDistance()
    }

    override fun setEdge(fromIndex: Int, toIndex: Int, newWeight: Double): Boolean {
        if (fromIndex == toIndex) {
            return false
        }
        val from = vertexes[fromIndex] ?: return false
        val to = vertexes[toIndex] ?: return false

        wholeOperation@while (true) {
            val newDescriptor = Descriptor(
                PriorityQueue(compareBy { it.first }),
                AtomicReference(IN_PROGRESS)
            )

            val fromDistance = from.acquireAndGet(newDescriptor)
            if (fromDistance == null) {
                newDescriptor.status.set(ABORTED)
                continue@wholeOperation
            }

            val toDistance = to.acquireAndGet(newDescriptor)
            if (toDistance == null) {
                newDescriptor.status.set(ABORTED)
                continue@wholeOperation
            }

            assert(to.descriptor.get().status.get() == IN_PROGRESS)
            assert(to.descriptor.get() === newDescriptor)

            val newToDistance = fromDistance + newWeight
            return if (newToDistance < toDistance) {
                from.outgoing[toIndex] = newWeight
                to.distance.set(Distance(oldDistance = toDistance, newDistance = newToDistance))
                to.changed.set(true)
                newDescriptor.queue.add(newToDistance to to)

                while (newDescriptor.queue.isNotEmpty()) {
                    val (curDistance, cur) = newDescriptor.queue.poll()
                    neighbors@for ((index, w) in cur.outgoing) {
                        val neighbour = vertexes[index]!!
                        if (neighbour === from) {
                            continue@neighbors
                        }
                        val oldDistance = neighbour.acquireAndGet(newDescriptor)
                            ?: if (neighbour.descriptor.get() === newDescriptor) {
                                neighbour.distance.get().newDistance
                            } else {
                                newDescriptor.status.set(ABORTED)
                                continue@wholeOperation
                            }
                        val newDistance = curDistance + w
                        if (newDistance < oldDistance) {
                            neighbour.distance.set(Distance(oldDistance = oldDistance, newDistance = newDistance))
                            neighbour.changed.set(true)
                            newDescriptor.queue.add(newDistance to neighbour)
                        } else {
                            neighbour.release()
                        }
                    }
                }
                newDescriptor.status.set(SUCCESS)
                return true
            } else {
                newDescriptor.status.set(ABORTED)
                false // only decremental for now
            }
        }
    }


    override fun addVertex(index: Int): Boolean {
        val newVertex = Vertex()
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