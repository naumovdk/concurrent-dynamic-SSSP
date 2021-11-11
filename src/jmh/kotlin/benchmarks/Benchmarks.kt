package benchmarks

import Dsssp
import Graph
import InputGraph
import benchmarks.util.Executor
import benchmarks.util.ScenarioGenerator
import concurrent.BasicConcurrentDsssp
import org.openjdk.jmh.annotations.*
import sequential.SequentialDsssp
import java.util.concurrent.TimeUnit
import concurrent.process.Process
import fit


@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
open class Benchmarks {
    @Param("1", "2", "4", "8", "16", "32", "64", "128")
//    @Param("1", "2", "4", "8")
    open var workers: Int = 0

    @Param("0.5", "0.8", "0.99")
    open var readWriteRatio: Double = 0.0

    @Param("USA", "WEST", "NY")
    open var graphName: String = ""

    @Param("0", "1")
    open var implIndex: Int = 0

    private val impls = listOf(
        { BasicConcurrentDsssp(onIntersection = Process::onIntersectionHelp) },
        { BasicConcurrentDsssp(onIntersection = Process::onIntersectionAbort) },
    )
    private val operations = listOf(
        10_000,
        10_000,
        100_000,
        100_000,
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
        impl = impls[implIndex].invoke()
        impl.fit(graph)
    }
}
