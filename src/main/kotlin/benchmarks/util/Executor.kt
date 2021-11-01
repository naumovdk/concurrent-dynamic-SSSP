package benchmarks.util

import Dsssp
import InputGraph
import java.util.concurrent.atomic.AtomicInteger

class BenchmarkThread(val threadId: Int, target: () -> Unit) : Thread(target)

class Executor(
    private val impl: Dsssp,
    private val threads: Int,
    private val operations: Int,
    private val readWriteRatio: Double,
    private val graph: InputGraph
) {
    fun run() {
        val cnt = AtomicInteger(0)
        val scenario = ScenarioGenerator.generate(operations, readWriteRatio, graph.nodes, graph.maxWeight)
        val ts = Array(threads) { threadId ->
            BenchmarkThread(threadId) {
                var i = cnt.incrementAndGet()
                while (i < operations) {
                    val u = scenario[3 * i]
                    val v = scenario[3 * i + 1]
                    val w = scenario[3 * i + 2]

                    val isRead = v == 0 && w == 0
                    if (isRead) {
                        impl.getDistance(v)
                    } else {
                        impl.setEdge(u, v, w.toDouble())
                    }
                    i = cnt.incrementAndGet()
                }
                println("Thread is finished")
            }
        }
        ts.forEach { it.start() }
        ts.forEach { it.join() }
    }
}