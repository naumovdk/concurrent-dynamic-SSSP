import concurrent.Concurrent
import org.jetbrains.kotlinx.lincheck.LoggingLevel
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.paramgen.DoubleGen
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.scenario
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.junit.jupiter.api.Test


@Param(name = "vertex", gen = IntGen::class, conf = "0:6")
class LinearizabilityTest {
    private val impl = Concurrent(0)

    @Operation(handleExceptionsAsResult = [IncrementalIsNotSupportedException::class])
    fun setEdge(
        @Param(name = "vertex") from: Int,
        @Param(name = "vertex") to: Int,
        @Param(gen = DoubleGen::class, conf = "1:3") newWeight: Double
    ) = impl.setEdge(from, to, newWeight)

    @Operation
    fun getDistance(@Param(name = "vertex") v: Int) = impl.getDistance(v)

    fun addVertex(@Param(name = "vertex") v: Int) = impl.addVertex(v)

    @Test
    fun singleThreadTest() = ModelCheckingOptions()
        .threads(1)
        .actorsBefore(16)
        .actorsPerThread(1)
        .sequentialSpecification(Recomputing::class.java)
        .logLevel(LoggingLevel.INFO)
        .minimizeFailedScenario(true)
        .check(LinearizabilityTest::class.java)

    @Test
    fun twoThreadsTest() = ModelCheckingOptions()
        .addCustomScenario(scenario {
            parallel {
                thread {
                    actor(Concurrent::setEdge, 3, 1, 1.0)
                }
                thread {
                    actor(Concurrent::setEdge, 0, 3, 1.0)
                }
            }
            post {
                actor(Concurrent::getDistance, 1)
            }
        })
        .threads(2)
        .actorsBefore(15)
        .actorsPerThread(3)
        .sequentialSpecification(Recomputing::class.java)
        .logLevel(LoggingLevel.INFO)
        .verboseTrace(true)
        .minimizeFailedScenario(true)
        .check(LinearizabilityTest::class.java)

    @Test
    fun modelCheckingTest() = ModelCheckingOptions()
        .threads(3)
        .actorsBefore(20)
        .actorsPerThread(3)
        .sequentialSpecification(Recomputing::class.java)
        .logLevel(LoggingLevel.INFO)
        .verboseTrace(true)
        .minimizeFailedScenario(true)
        .check(this::class.java)

    @Test
    fun stressTest() = StressOptions()
        .threads(4)
        .iterations(100)
        .actorsBefore(50)
        .actorsPerThread(3)
        .sequentialSpecification(Recomputing::class.java)
        .logLevel(LoggingLevel.INFO)
        .minimizeFailedScenario(true)
        .check(LinearizabilityTest::class.java)

}