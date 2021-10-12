package benchmarks.util

import Dsssp
import INITIAL_SIZE
import kotlin.random.Random.Default.nextDouble

class BenchmarkThread(val threadId: Int, target: () -> Unit) : Thread(target)

class Executor(private val impl: Dsssp, private val threads: Int, private val operations: Int) {
    fun run() {
        val per = operations / threads
        val ts = Array(threads) { threadId ->
            BenchmarkThread(threadId) {
                for (i in 0..per) {
                    val r = nextDouble()
                    val u = (0..INITIAL_SIZE).random()
                    val v = (0..INITIAL_SIZE).random()
                    when {
                        r < 0.5 -> {
                            impl.setEdge(u, v, r)
                        }
                        r >= 0.5 -> {
                            impl.getDistance(u)
                        }
                    }
                }
            }
        }
        ts.forEach { it.start() }
    }
}