package benchmarks

import Dsssp
import SequentialTest
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
@BenchmarkMode(Mode.All)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 2, jvmArgs = ["-Xms2G", "-Xmx2G"])
open class SmallBenchmark {
    @Param("1", "2", "4", "8", "16", "32", "64", "128")
    open var workers: Int = 0

    @Param("0.5", "0.9", "0.99")
    open var readProbability: Double = 0.0

    private val operations = 1

    private fun benchmark(impl: Dsssp) {
        Executor(impl, workers, operations, readProbability).run()
    }

    @Benchmark
    fun benchmarkDijkstraRecomputing() {
        benchmark(DijkstraRecomputing())
    }

    @Benchmark
    fun benchmarkSequentialDsssp() {
        benchmark(SequentialDsssp())
    }

    @Benchmark
    fun benchmarkPanigraham() {
        benchmark(Panigraham())
    }

    @Benchmark
    fun benchmarkBasicConcurrentDsssp() {
        benchmark(BasicConcurrentDsssp())
    }
}

@Throws(RunnerException::class)
fun main() {
    Runner(OptionsBuilder().
        include(SequentialTest::class.java.simpleName)
        .resultFormat(ResultFormatType.CSV)
        .result("results_50_50.csv")
        .build())
}