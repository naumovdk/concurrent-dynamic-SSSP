package benchmarks.util

import Dsssp
import INITIAL_SIZE
import kotlin.random.Random.Default.nextDouble

class BenchmarkThread(val threadId: Int, target: () -> Unit) : Thread(target)

class Executor(
    private val impl: Dsssp,
    private val threads: Int,
    private val operations: Int,
    private val readProbability: Double
) {
    fun run() {
        val per = if (operations / threads != 0) operations / threads else 1
        val ts = Array(threads) { threadId ->
            BenchmarkThread(threadId) {
                for (i in 0..per) {
                    val r = nextDouble()
                    val u = (0..INITIAL_SIZE).random()
                    val v = (0..INITIAL_SIZE).random()
                    when {
                        r < readProbability -> {
                            impl.getDistance(u)
                        }
                        else -> {
                            impl.setEdge(u, v, r)
                        }
                    }
                }
            }
        }
        ts.forEach { it.start() }
    }
}