package benchmarks.util

import kotlin.random.Random

class ScenarioGenerator {
    companion object {
        fun generate(size: Int, readWriteRatio: Double, nodes: Int, maxWeight: Int, seed: Int): List<Int> {
            val r = Random(seed)
            val res = mutableListOf<Int>()
            repeat(size) {
                val u = (0 until nodes).random()
                if (r.nextDouble() < readWriteRatio) {
                    res.add(u)
                    res.add(0)
                    res.add(0)
                } else {
                    val v = r.nextInt(nodes)
                    val w = r.nextInt(maxWeight)
                    res.add(u)
                    res.add(v)
                    res.add(w)
                }
            }
            return res
        }
    }
}