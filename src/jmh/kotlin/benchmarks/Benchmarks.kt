package benchmarks

import Dsssp
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
import kotlin.jvm.Throws


@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@Measurement(iterations = 1, time = 3, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 0)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
open class SmallBenchmark {
//    @Param("1", "2", "4", "8", "16", "32", "64", "128")
    @Param("1")
    open var workers: Int = 0

    @Param("0.5", "0.9", "0.99")
    open var readProbability: Double = 0.0

    @Param("NY")
    open var graphName: String = ""

    @Param("1", "2")
    open var implIndex: Int = 0

    private val impls = listOf(
        { g: InputGraph -> BasicConcurrentDsssp(g) },
        { g: InputGraph -> SequentialDsssp(g) },
        { g: InputGraph -> DijkstraRecomputing(g) })

    var impl: Dsssp? = null
    var graph: InputGraph? = null

    private val operations = 1

    private fun benchmark() {
        Executor(impl!!, workers, operations, readProbability, graph!!).run()
    }

    @Setup(Level.Invocation)
    fun initialize() {
        graph = Graph.getGraph(graphName)
        impl = impls[implIndex](graph!!)
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