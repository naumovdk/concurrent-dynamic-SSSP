import concurrent.BasicConcurrentDsssp
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
import sequential.SequentialDsssp


@Param(name = "vertex", gen = IntGen::class, conf = "0:${INITIAL_SIZE}")
class LinearizabilityTest {
    private val emptyGraph = InputGraph(INITIAL_SIZE, listOf(), 0, 0)
    private val impl = BasicConcurrentDsssp(emptyGraph)
    private val seq = SequentialDsssp(emptyGraph)

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
        .actorsBefore(150)
        .actorsPerThread(1)
        .actorsAfter(0)
        .sequentialSpecification(seq::class.java)
        .logLevel(LoggingLevel.INFO)
        .hangingDetectionThreshold(10)
        .minimizeFailedScenario(true)
        .check(LinearizabilityTest::class.java)

    @Test
    fun twoThreadsTest() = ModelCheckingOptions()
        .addCustomScenario(scenario {
            parallel {
                thread {
                    actor(Dsssp::setEdge, 3, 1, 1.0)
                    actor(Dsssp::getDistance, 1)
                    actor(Dsssp::getDistance, 0)
                }
                thread {
                    actor(Dsssp::setEdge, 0, 3, 1.0)
                }
            }
        })
        .addCustomScenario {
            initial {
                actor(Dsssp::setEdge, 0, 1, 1.0)
                actor(Dsssp::setEdge, 4, 2, 1.0)
            }
            parallel {
                thread {
                    actor(Dsssp::setEdge, 3, 4, 1.0)
                }
                thread {
                    actor(Dsssp::setEdge, 4, 5, 1.0)
                }
            }
            post {
                actor(Dsssp::getDistance, 4)
            }
        }
        .addCustomScenario {
            initial {
                actor(Dsssp::setEdge, 5, 3, 3.0)
                actor(Dsssp::setEdge, 4, 1, 3.0)
                actor(Dsssp::setEdge, 3, 4, 9.0)
                actor(Dsssp::setEdge, 3, 1, 39.0)
            }
            parallel {
                thread {
                    actor(Dsssp::setEdge, 0, 5, 27.0)
                }
                thread {
                    actor(Dsssp::getDistance, 1)
                }
            }
        }
        .addCustomScenario {
            initial {
                actor(Dsssp::setEdge, 1,3,35.0)
                actor(Dsssp::setEdge, 2,1,13.0)
            }
            parallel {
                thread {
                    actor(Dsssp::setEdge, 0,2,13.0)
                }
                thread {
                    actor(Dsssp::setEdge, 2,1,19.0)
                }
            }
            post {
                actor(Dsssp::getDistance, 1)
                actor(Dsssp::getDistance, 3)
            }
        }
        .addCustomScenario {
            parallel {
                thread {
                    actor(Dsssp::setEdge, 0,3,23.0)
                }
                thread {
                    actor(Dsssp::setEdge, 3,1,11.0)
                }
                thread {
                    actor(Dsssp::setEdge, 1,6,9.0)
                    actor(Dsssp::getDistance, 3)
                }
            }
            post {
                actor(Dsssp::getDistance, 6)
            }
        }
        .threads(2)
        .actorsBefore(22)
        .actorsPerThread(2)
        .actorsAfter(10)
        .sequentialSpecification(seq::class.java)
        .logLevel(LoggingLevel.INFO)
        .verboseTrace(true)
        .minimizeFailedScenario(true)
        .check(LinearizabilityTest::class.java)

    @Test
    fun modelCheckingTest() = ModelCheckingOptions()
        .threads(3)
        .actorsBefore(15)
        .actorsPerThread(3)
        .actorsAfter(15)
        .sequentialSpecification(seq::class.java)
        .logLevel(LoggingLevel.INFO)
        .minimizeFailedScenario(true)
        .check(this::class.java)

    @Test
    fun stressTest() = StressOptions()
        .threads(3)
        .actorsBefore(35)
        .actorsPerThread(2)
        .actorsAfter(10)
        .sequentialSpecification(seq::class.java)
        .logLevel(LoggingLevel.INFO)
        .minimizeFailedScenario(true)
        .check(LinearizabilityTest::class.java)
}