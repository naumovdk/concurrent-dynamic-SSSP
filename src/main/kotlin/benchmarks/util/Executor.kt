package benchmarks.util

import Dsssp

class Executor(
    private val impl: Dsssp,
    scenarios: Array<List<Int>>
) {
    @Volatile
    private var start = false

    private val threads: Array<Thread>

    init {
        @Suppress("ControlFlowWithEmptyBody")
        threads = scenarios.map {
            Thread {
                while (!start); // wait
                var i = 0
                while (3 * i + 2 < it.size) {
                    val u = it[3 * i]
                    val v = it[3 * i + 1]
                    val w = it[3 * i + 2]

                    val isRead = v == 0 && w == 0
                    if (isRead) {
                        impl.getDistance(v)
                    } else {
                        impl.setEdge(u, v, w.toDouble())
                    }
                    i++
                }
            }
        }.toTypedArray()
        threads.forEach { it.start() }
    }

    fun run() {
        start = true
        threads.forEach { it.join() }
    }
}