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
    private val impl = BasicConcurrentDsssp()
    private val seq = SequentialDsssp()

    @Operation
    fun setEdge(
        @Param(name = "vertex") from: Int,
        @Param(name = "vertex") to: Int,
        @Param(gen = DoubleGen::class, conf = "1:3") newWeight: Double
    ) = impl.setEdge(from, to, newWeight)

    @Operation
    fun getDistance(@Param(name = "vertex") v: Int) = impl.getDistance(v)

    @Test
    fun singleThreadTest() = StressOptions()
        .threads(1)
        .actorsBefore(40)
        .actorsPerThread(1)
        .actorsAfter(0)
        .sequentialSpecification(seq::class.java)
        .logLevel(LoggingLevel.INFO)
        .minimizeFailedScenario(true)
        .check(LinearizabilityTest::class.java)

    @Test
    fun twoThreadsTest() = ModelCheckingOptions()
        
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
        .actorsBefore(25)
        .actorsPerThread(3)
        .actorsAfter(15)
        .verboseTrace(true)
        .sequentialSpecification(seq::class.java)
        .logLevel(LoggingLevel.INFO)
        .minimizeFailedScenario(true)
        .check(this::class.java)

    @Test
    fun stressTest() = StressOptions()
        .threads(3)
        .actorsBefore(40)
        .actorsPerThread(2)
        .actorsAfter(15)
        .sequentialSpecification(seq::class.java)
        .logLevel(LoggingLevel.INFO)
        .iterations(1000)
        .minimizeFailedScenario(true)
        .check(LinearizabilityTest::class.java)

    @Test
    fun cornerCases() = ModelCheckingOptions()
        .addCustomScenario {
            initial {
                actor(Dsssp::setEdge, 1, 2, 1.0)
                actor(Dsssp::setEdge, 0, 2, 29.0)
            }
            parallel {
                thread {
                    actor(Dsssp::setEdge, 0, 2, 19.0)
                }
                thread {
                    actor(Dsssp::setEdge, 2, 1, 5.0)
                }
            }
            post {
                actor(Dsssp::getDistance, 0)
                actor(Dsssp::getDistance, 1)
                actor(Dsssp::getDistance, 2)
            }
        }
        .addCustomScenario {
            parallel {
                thread {
                    actor(Dsssp::setEdge, 0, 2, 17.0)
                }
                thread {
                    actor(Dsssp::setEdge, 1, 2, 11.0)
                }
            }
            post {
                actor(Dsssp::getDistance, 2)
            }
        }
        .addCustomScenario {
            parallel {
                thread {
                    actor(Dsssp::setEdge, 0, 2, 19.0)
                }
                thread {
                    actor(Dsssp::setEdge, 2, 1, 5.0)
                }
            }
            post {
                actor(Dsssp::getDistance, 2)
            }
        }.addCustomScenario(scenario {
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
                actor(Dsssp::setEdge, 1, 3, 35.0)
                actor(Dsssp::setEdge, 2, 1, 13.0)
            }
            parallel {
                thread {
                    actor(Dsssp::setEdge, 0, 2, 13.0)
                }
                thread {
                    actor(Dsssp::setEdge, 2, 1, 19.0)
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
                    actor(Dsssp::setEdge, 0, 3, 23.0)
                }
                thread {
                    actor(Dsssp::setEdge, 3, 1, 11.0)
                }
                thread {
                    actor(Dsssp::setEdge, 1, 6, 9.0)
                    actor(Dsssp::getDistance, 3)
                }
            }
            post {
                actor(Dsssp::getDistance, 6)
            }
        }
        .addCustomScenario {
            initial {
                actor(Dsssp::setEdge, 2, 1, 23.0)
            }
            parallel {
                thread {
                    actor(Dsssp::setEdge, 0, 1, 9.0)
                }
                thread {
                    actor(Dsssp::setEdge, 0, 2, 35.0)
                }
            }
            post {
                actor(Dsssp::getDistance, 0)
                actor(Dsssp::getDistance, 1)
                actor(Dsssp::getDistance, 2)
            }
        }        .addCustomScenario {
            initial {
                actor(Dsssp::setEdge, 0, 1, 33.0)
                actor(Dsssp::setEdge, 9, 10, 17.0)
                actor(Dsssp::setEdge, 2, 8, 13.0)
                actor(Dsssp::setEdge, 10, 7, 15.0)
                actor(Dsssp::setEdge, 1, 2, 7.0)
            }
            parallel {
                thread {
                    actor(Dsssp::setEdge, 2, 9, 25.0)
                }
                thread {
                    actor(Dsssp::setEdge, 8, 9, 29.0)
                }
            }
            post {
                actor(Dsssp::getDistance, 7)
            }
        }
        .addCustomScenario {
            initial {
                actor(Dsssp::setEdge, 1, 2, 5.0)
                actor(Dsssp::setEdge, 2, 4, 3.0)
            }
            parallel {
                thread {
                    actor(Dsssp::setEdge, 1, 3, 37.0)
                }
                thread {
                    actor(Dsssp::setEdge, 0, 1, 33.0)
                }
            }
            post {
                actor(Dsssp::getDistance, 4)
                actor(Dsssp::getDistance, 3)
                actor(Dsssp::getDistance, 2)
                actor(Dsssp::getDistance, 1)
                actor(Dsssp::getDistance, 0)
            }
        }
        .addCustomScenario {
            initial {
                actor(Dsssp::setEdge, 2, 4, 17.0)
                actor(Dsssp::setEdge, 3, 2, 7.0)
            }
            parallel {
                thread {
                    actor(Dsssp::setEdge, 1, 0, 27.0)
                }
                thread {
                    actor(Dsssp::setEdge, 0, 3, 27.0)
                }
            }
        }
        .addCustomScenario {
            initial {
                actor(Dsssp::setEdge, 1, 2, 1.0)
                actor(Dsssp::setEdge, 0, 2, 29.0)
            }
            parallel {
                thread {
                    actor(Dsssp::setEdge, 0, 2, 19.0)
                }
                thread {
                    actor(Dsssp::setEdge, 2, 1, 5.0)
                }
            }
            post {
                actor(Dsssp::getDistance, 0)
                actor(Dsssp::getDistance, 1)
                actor(Dsssp::getDistance, 2)
            }
        }
        .addCustomScenario {
            parallel {
                thread {
                    actor(Dsssp::setEdge, 0, 2, 17.0)
                }
                thread {
                    actor(Dsssp::setEdge, 1, 2, 11.0)
                }
            }
            post {
                actor(Dsssp::getDistance, 2)
            }
        }
        .addCustomScenario {
            parallel {
                thread {
                    actor(Dsssp::setEdge, 0, 2, 19.0)
                }
                thread {
                    actor(Dsssp::setEdge, 2, 1, 5.0)
                }
            }
            post {
                actor(Dsssp::getDistance, 2)
            }
        }
        .sequentialSpecification(seq::class.java)
        .verboseTrace(true)
        .check(LinearizabilityTest::class.java)
}