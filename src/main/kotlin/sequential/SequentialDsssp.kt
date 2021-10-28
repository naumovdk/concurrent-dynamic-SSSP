package sequential

import Dsssp
import INITIAL_SIZE
import InputGraph
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState
import java.util.*
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.collections.HashMap

class SequentialDsssp(inputGraph: InputGraph, source: Int = 0) : Dsssp(inputGraph) {
    class Vertex(
        var distance: Double = POSITIVE_INFINITY,
        val outgoing: HashMap<Int, Double> = hashMapOf(),
        val incoming: HashMap<Int, Double> = hashMapOf(),
        var parent: Vertex? = null,
        var children: MutableSet<Vertex> = mutableSetOf()
    )

    private val vertexes = HashMap<Int, Vertex>()

    init {
        vertexes[source] = Vertex(0.0)

        for (i in 0..INITIAL_SIZE) {
            addVertex(i)
        }
    }

    @Synchronized
    override fun getDistance(index: Int): Int? {
        return vertexes[index]?.distance?.toInt()
    }

    @Synchronized
    override fun setEdge(fromIndex: Int, toIndex: Int, newWeight: Double): Boolean {
        if (fromIndex == toIndex) return false
        val from = vertexes[fromIndex] ?: return false
        val to = vertexes[toIndex] ?: return false

        from.outgoing[toIndex] = newWeight
        to.incoming[fromIndex] = newWeight

        val oldToDistance = to.distance
        val offeredDistance = from.distance + newWeight

        val priorityQueue = PriorityQueue<Vertex>(compareBy { it.distance })

        if (offeredDistance > to.distance && to.parent === from && Dsssp.supportInc) {
            val workSet = mutableSetOf(to)
            val affected = mutableSetOf<Vertex>()
            while (workSet.isNotEmpty()) {
                val cur = workSet.first().also { workSet.remove(it) }

                cur.distance = Dsssp.INF
                affected.add(cur)
                workSet.addAll(cur.children)

                cur.parent = null
                cur.children.clear()
            }
            val starting = mutableSetOf<Vertex>()
            affected.forEach {
                starting.addAll(it.incoming.keys.map { index -> vertexes[index]!! })
            }

            priorityQueue.addAll(starting)
        }
        if (offeredDistance < oldToDistance) {
            to.distance = offeredDistance
            to.parent = from
            to.children.remove(from)
            from.children.add(to)

            priorityQueue.add(to)
        }
        while (Dsssp.supportDec && priorityQueue.isNotEmpty()) {
            val cur = priorityQueue.poll()
            cur.outgoing.forEach { (i, w) ->
                val neighbor = vertexes[i]!!
                if (cur.distance + w < neighbor.distance) {
                    neighbor.distance = cur.distance + w
                    neighbor.parent = cur
                    neighbor.children.remove(cur)
                    cur.children.add(neighbor)

                    priorityQueue.add(neighbor)
                }
            }
        }

        return true
    }

    @Synchronized
    override fun addVertex(index: Int): Boolean {
        val new = Vertex()
        val mapped = vertexes.getOrPut(index) { new }
        return new === mapped
    }

    override fun removeEdge(fromIndex: Int, toIndex: Int): Boolean {
        TODO()
    }

    override fun removeVertex(index: Int): Boolean {
        TODO()
    }

//    override fun extractState(): Any {
//        return vertexes.map { (i, v) -> i to v.distance to v.outgoing }
//    }
}