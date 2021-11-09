package benchmarks

import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.RunnerException
import org.openjdk.jmh.runner.options.OptionsBuilder


@Throws(RunnerException::class)
fun main() {
    Runner(
        OptionsBuilder().include(Benchmarks::class.java.simpleName)
            .resultFormat(ResultFormatType.CSV)
            .result("results.csv")
            .build()
    ).run()
}