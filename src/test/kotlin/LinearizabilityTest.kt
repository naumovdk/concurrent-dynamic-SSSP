import org.jetbrains.kotlinx.lincheck.LoggingLevel
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.paramgen.DoubleGen
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.scenario
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressCTest
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState
import org.junit.Test


@Param(name = "vertex", gen = IntGen::class, conf = "0:6")
class LinearizabilityTest : VerifierState() {
    private val impl = DssspImpl(0)

    @Operation
    public fun setEdge(
        @Param(name = "vertex") from: Int,
        @Param(name = "vertex") to: Int,
        @Param(gen = DoubleGen::class, conf = "1:3") newWeight: Double
    ) = impl.setEdge(from, to, newWeight)

    @Operation
    public fun getDistance(@Param(name = "vertex") v: Int) = impl.getDistance(v)

    public fun addVertex(@Param(name = "vertex") v: Int) = impl.addVertex(v)

    @Test
    fun modelCheckingTest() = ModelCheckingOptions()
        .threads(3)
        .actorsPerThread(8)
        .sequentialSpecification(Dijkstra::class.java)
        .logLevel(LoggingLevel.INFO)
        .verboseTrace(true)
        .minimizeFailedScenario(true)
        .check(this::class.java)

    @Test
    fun stressTest() = StressOptions()
        .threads(4)
        .iterations(150)
        .actorsBefore(20)
        .actorsPerThread(3)
        .sequentialSpecification(Dijkstra::class.java)
        .logLevel(LoggingLevel.INFO)
        .minimizeFailedScenario(true)
        .check(LinearizabilityTest::class.java)

    @Test
    fun singleThreadTest() = StressOptions()
        .threads(1)
        .actorsBefore(100)
        .actorsPerThread(1)
        .sequentialSpecification(Dijkstra::class.java)
        .logLevel(LoggingLevel.INFO)
        .minimizeFailedScenario(true)
        .check(LinearizabilityTest::class.java)


    override fun extractState(): Any {
        return impl
    }
}
