package concurrent

import Dsssp
import INITIAL_SIZE
import com.google.gson.Gson
import concurrent.process.Process
import concurrent.process.Status
import concurrent.vertex.Vertex
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KFunction2

class BasicConcurrentDsssp(source: Int = 0, val onIntersection: KFunction2<Process, Process, Unit> = Process::onIntersectionHelp) : Dsssp {
    private val vertexes = ConcurrentHashMap<Int, Vertex>()

    init {
        vertexes[source] = Vertex(0.0)

        for (i in 0..INITIAL_SIZE) {
            addVertex(i)
        }
    }

    override fun getDistance(index: Int): Double? {
        val vertex = vertexes[index] ?: return null
        return vertex.getDistance().distance
    }

    override fun setEdge(fromIndex: Int, toIndex: Int, newWeight: Double): Boolean {
        if (fromIndex == toIndex) return false
        val from = vertexes[fromIndex] ?: return false
        val to = vertexes[toIndex] ?: return false

        val threadId = Thread.currentThread().id

        while (true) {
            val process = Process(threadId, newWeight, onIntersectionPolicy = onIntersection)
            process.from = from
            process.to = to

            process.help()

            if (process.status.value == Status.SUCCESS) {
                return true
            }
            process.status.getAndSet(Status.ABORTED)
            assert(process.status.value == Status.ABORTED)
        }
    }

    fun copy(): BasicConcurrentDsssp {
        val json = Gson().toJson(this)
        return Gson().fromJson(json, BasicConcurrentDsssp::class.java)
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
