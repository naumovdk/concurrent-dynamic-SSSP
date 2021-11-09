package benchmarks

import Dsssp
import Graph
import InputGraph
import bapi.Panigraham
import benchmarks.util.Executor
import benchmarks.util.ScenarioGenerator
import concurrent.BasicConcurrentDsssp
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.RunnerException
import org.openjdk.jmh.runner.options.OptionsBuilder
import sequential.DijkstraRecomputing
import sequential.SequentialDsssp
import java.util.concurrent.TimeUnit


@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
open class Benchmarks {
    @Param("1", "2", "4", "8", "16", "32", "64", "128")
//    @Param("1", "2", "4", "8")
    open var workers: Int = 0

    @Param("1")
    open var readWriteRatio: Double = 0.0

    @Param("NY")
    open var graphName: String = ""

    @Param("0")
    open var implIndex: Int = 0

    private val impls = listOf(
        { BasicConcurrentDsssp() },
        { DijkstraRecomputing() },
        { Panigraham() },
        { SequentialDsssp() }
    )
    private val operations = listOf(
        1_000_000,
        10_000,
        10_000,
        1000_000
    )

    private var graph: InputGraph = Graph.emptyGraph
    private var impl: Dsssp = BasicConcurrentDsssp()
    private var executor: Executor = Executor(impl, arrayOf())

    @Benchmark
    fun benchmark() = executor.run()

    @Setup(Level.Trial)
    fun setupGraph() {
        graph = Graph.getGraph(graphName)
    }

    @Setup(Level.Invocation)
    fun setupThreads() {
        val operationsPerThread = operations[implIndex] / workers
        val scenarios = (0 until workers).map { seed ->
            ScenarioGenerator.generate(
                operationsPerThread,
                readWriteRatio,
                graph.nodes,
                graph.maxWeight,
                seed
            )
        }.toTypedArray()
        executor = Executor(impl, scenarios)
    }

    @Setup(Level.Invocation)
    fun fit() {
        impl = impls[implIndex].invoke().fit(graph)
    }
}
