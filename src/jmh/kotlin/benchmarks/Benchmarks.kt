package benchmarks

import Dsssp
import Graph
import InputGraph
import bapi.Panigraham
import benchmarks.util.Executor
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
@BenchmarkMode(Mode.AverageTime, Mode.Throughput)
@Measurement(iterations = 4, time = 3, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 0)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
open class SmallBenchmark {
    @Param("1", "2", "4", "8", "16", "32", "64", "128")
    open var workers: Int = 0

    @Param("0.5", "0.9", "0.99")
    open var readWriteRatio: Double = 0.0

    @Param("WEST", "NY")
    open var graphName: String = ""

    @Param("0", "1", "2", "3")
    open var implIndex: Int = 0

    private val impls = listOf(
        { BasicConcurrentDsssp() },
        { Panigraham() },
        { SequentialDsssp() },
        { DijkstraRecomputing() }
    )

    private val operations = 100
    private var graph: InputGraph? = null
    private var impl: Dsssp? = null

    @Benchmark
    fun benchmark() =
        Executor(impl!!, workers, operations, readWriteRatio, graph!!).run()

    @Setup(Level.Trial)
    fun f() {
        graph = Graph.getGraph(graphName)
        impl = impls[implIndex].invoke().fit(graph!!)
    }
}

@Throws(RunnerException::class)
fun main() {
    Runner(
        OptionsBuilder().include(SmallBenchmark::class.java.simpleName)
            .resultFormat(ResultFormatType.CSV)
            .result("results.csv")
            .build()
    )
}