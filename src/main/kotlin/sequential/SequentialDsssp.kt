package sequential

import Dsssp
import Dsssp.Companion.INF
import Dsssp.Companion.supportDecremental
import Dsssp.Companion.supportIncremental
import INITIAL_SIZE
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState
import java.io.BufferedWriter
import java.io.FileWriter
import java.util.*
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.collections.HashMap

class SequentialDsssp(source: Int = 0) : Dsssp, VerifierState() {
    class Vertex(
        var distance: Double = POSITIVE_INFINITY,
        val outgoing: HashMap<Int, Double> = hashMapOf(),
        val incoming: HashMap<Int, Double> = hashMapOf(),
        var parent: Vertex? = null
    )

    private val vertexes = HashMap<Int, Vertex>()

    init {
        vertexes[source] = Vertex(0.0)

        for (i in 0..INITIAL_SIZE) {
            addVertex(i)
        }
    }

    @Synchronized
    override fun getDistance(index: Int): Double? {
        return vertexes[index]?.distance
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

        if (offeredDistance < oldToDistance) {
            to.distance = offeredDistance
            to.parent = from
        }
        if (offeredDistance > to.distance && to.parent === from && supportIncremental) {
            val workSet = mutableSetOf(to)
            val affected = mutableSetOf<Vertex>()
            while (workSet.isNotEmpty()) {
                val cur = workSet.first().also { workSet.remove(it) }

                cur.distance = INF
                cur.parent = null

                affected.add(cur)

                for ((i, _) in cur.outgoing) {
                    val u = vertexes[i]!!
                    if (u.parent === cur) {
                        workSet.add(u)
                    }
                }
            }
            val starting = mutableSetOf<Vertex>()
            affected.forEach {
                starting.addAll(it.incoming.keys.map { index -> vertexes[index]!! })
            }

            priorityQueue.addAll(starting)
        }
        if (priorityQueue.isEmpty()) {
            priorityQueue.add(to)
        }
        while (supportDecremental && priorityQueue.isNotEmpty()) {
            val cur = priorityQueue.poll()
            cur.outgoing.forEach { (i, w) ->
                val neighbor = vertexes[i]!!
                if (cur.distance + w < neighbor.distance) {
                    neighbor.distance = cur.distance + w
                    neighbor.parent = cur

                    priorityQueue.add(neighbor)
                }
            }
        }

        return true
    }

    fun printSsspTree(path: String) {
//        val file = Files.createFile(Paths.get(path))
        val writer = BufferedWriter(FileWriter(path))
        for ((i, v) in vertexes) {
            writer.write("$i ${v.parent ?: "-"} ${v.distance.toInt()}")
            for ((u, w) in v.outgoing) {
                writer.write("o $u ${w.toInt()}")
            }
            for ((u, w) in v.incoming) {
                writer.write("i $u ${w.toInt()}")
            }
        }
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

    override fun extractState(): Any {
        return vertexes.map { (i, v) -> i to v.distance to v.outgoing }
    }
}