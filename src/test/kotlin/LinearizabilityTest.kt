import concurrent.ConcurrentDsssp
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


@Param(name = "vertex", gen = IntGen::class, conf = "0:${INITIAL_SIZE}")
class LinearizabilityTest {
    private val impl = ConcurrentDsssp(0)

    @Operation
    fun setEdge(
        @Param(name = "vertex") from: Int,
        @Param(name = "vertex") to: Int,
        @Param(gen = DoubleGen::class, conf = "1:3") newWeight: Double
    ) = impl.setEdge(from, to, newWeight)

    @Operation
    fun getDistance(@Param(name = "vertex") v: Int) = impl.getDistance(v)

    @Test
    fun singleThreadTest() = ModelCheckingOptions()
        .threads(1)
        .actorsBefore(5)
        .actorsPerThread(1)
        .actorsAfter(3)
        .sequentialSpecification(SequentialDsssp::class.java)
        .logLevel(LoggingLevel.INFO)
        .minimizeFailedScenario(true)
        .check(LinearizabilityTest::class.java)

    @Test
    fun twoThreadsTest() = ModelCheckingOptions()
        .addCustomScenario(scenario {
            parallel {
                thread {
                    actor(ConcurrentDsssp::setEdge, 3, 1, 1.0)
                    actor(ConcurrentDsssp::getDistance, 1)
                }
                thread {
                    actor(ConcurrentDsssp::setEdge, 0, 3, 1.0)
                }
            }
        })
        .addCustomScenario {
            initial {
                actor(ConcurrentDsssp::setEdge, 0, 1, 1.0)
                actor(ConcurrentDsssp::setEdge, 4, 2, 1.0)
            }
            parallel {
                thread {
                    actor(ConcurrentDsssp::setEdge, 3, 4, 1.0)
                }
                thread {
                    actor(ConcurrentDsssp::setEdge, 4, 5, 1.0)
                }
            }
            post {
                actor(ConcurrentDsssp::getDistance, 4)
            }
        }
        .addCustomScenario {
            initial {
                actor(ConcurrentDsssp::setEdge, 5, 3, 3.0)
                actor(ConcurrentDsssp::setEdge, 4, 1, 3.0)
                actor(ConcurrentDsssp::setEdge, 3, 4, 9.0)
                actor(ConcurrentDsssp::setEdge, 3, 1, 39.0)
            }
            parallel {
                thread {
                    actor(ConcurrentDsssp::setEdge, 0, 5, 27.0)
                }
                thread {
                    actor(ConcurrentDsssp::getDistance, 1)
                }
            }
        }
        .threads(2)
        .actorsBefore(15)
        .actorsPerThread(3)
        .actorsAfter(25)
        .sequentialSpecification(SequentialDsssp::class.java)
        .logLevel(LoggingLevel.INFO)
        .verboseTrace(true)
        .minimizeFailedScenario(true)
        .check(LinearizabilityTest::class.java)

    @Test
    fun modelCheckingTest() = ModelCheckingOptions()
        .threads(4)
        .actorsBefore(15)
        .actorsPerThread(3)
        .actorsAfter(15)
        .sequentialSpecification(SequentialDsssp::class.java)
        .logLevel(LoggingLevel.INFO)
        .minimizeFailedScenario(true)
        .check(this::class.java)

    @Test
    fun stressTest() = StressOptions()
        .threads(4)
        .actorsBefore(15)
        .actorsPerThread(3)
        .actorsAfter(15)
        .sequentialSpecification(SequentialTest::class.java)
        .logLevel(LoggingLevel.INFO)
        .minimizeFailedScenario(true)
        .check(LinearizabilityTest::class.java)
}