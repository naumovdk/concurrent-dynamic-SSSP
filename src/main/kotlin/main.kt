import bapi.Panigraham
import benchmarks.util.Executor
import benchmarks.util.ScenarioGenerator
import concurrent.BasicConcurrentDsssp
import sequential.SequentialDsssp

fun main() {
    val graph = Graph.getGraph("NY")
    val scenarios = (0 until 1).map { seed ->
        ScenarioGenerator.generate(
            1000000,
            0.99,
            graph.nodes,
            graph.maxWeight,
            seed
        )
    }.toTypedArray()
    Executor(Panigraham().fit(graph), scenarios).run()
}