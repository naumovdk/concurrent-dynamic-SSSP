package benchmarks.util

import kotlin.random.Random.Default.nextDouble

class ScenarioGenerator {
    companion object {
        fun generate(size: Int, readWriteRatio: Double, nodes: Int, maxWeight: Int): List<Int> {
            val res = mutableListOf<Int>()
            repeat(size) {
                val u = (0 until nodes).random()
                if (nextDouble() < readWriteRatio) {
                    res.add(u)
                    res.add(0)
                    res.add(0)
                } else {
                    val v = (0 until nodes).random()
                    val w = (0 until maxWeight).random()
                    res.add(u)
                    res.add(v)
                    res.add(w)
                }
            }
            return res
        }
    }
}