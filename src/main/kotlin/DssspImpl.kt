import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock

class DssspImpl(graph: Graph, source: Int) : Dsssp() {
    class Vertex(val index: Int) {
        val outgoing = ConcurrentHashMap<Int, Double>()
        var distance = AtomicReference(Double.POSITIVE_INFINITY)
        var descriptorRef: AtomicReference<DescriptorRef> = AtomicReference(DescriptorRef(AtomicReference(null)))
        val lock = ReentrantLock()
        val version = AtomicInteger(0)
    }

    data class DescriptorRef(val ref: AtomicReference<Descriptor?>)

    data class Descriptor(val queue: PriorityQueue<Vertex>)

    private val processes = Runtime.getRuntime().availableProcessors()
    private val vertexes = ConcurrentHashMap<Int, Vertex>()

    init {
        vertexes[source] = Vertex(source)
        vertexes[source]!!.distance.set(0.0)
    }

    override fun getDistance(index: Int): Double? {
        val vertex = vertexes[index] ?: return null
        while (true) {
            val firstReadVersion = vertex.version.get()
            val descriptorRef = vertex.descriptorRef.get()
            val result = vertex.distance.get()
            val secondReadVersion = vertex.version.get()
            if (firstReadVersion == secondReadVersion && descriptorRef.ref.get() == null) {
                return result
            }
        }
    }

    override fun setEdge(fromIndex: Int, toIndex: Int, newWeight: Double): Boolean {
        val from = vertexes[fromIndex] ?: return false
        val to = vertexes[toIndex] ?: return false
        assert(newWeight < from.outgoing.getOrDefault(toIndex, Double.POSITIVE_INFINITY))

        val descriptor = Descriptor(PriorityQueue(compareBy<Vertex> { it.distance.get() }))
        val descriptorRef = DescriptorRef(AtomicReference(descriptor))
        to.descriptorRef.set(descriptorRef)
        descriptor.queue.add(to)

        from.outgoing[toIndex] = newWeight

        while (descriptor.queue.isNotEmpty()) {
            val cur = descriptor.queue.poll()
            cur.outgoing.forEach { (index, w) ->
                val u = vertexes[index]!!
                val uDesc = u.descriptorRef.get().ref.get()
                while (true) {
                    if (u.descriptorRef.compareAndSet(null, descriptorRef)) {
                        break
                    }
                }
                val candidateDistance = cur.distance.get() + w
                if (candidateDistance < u.distance.get()) {
                    u.distance.set(candidateDistance)
                }
            }
        }

    }

    override fun removeEdge(from: Int, to: Int): Boolean {
        TODO()
    }

    override fun addVertex(index: Int): Boolean {
        TODO()
    }

    override fun removeVertex(index: Int): Boolean {
        TODO()
    }
}