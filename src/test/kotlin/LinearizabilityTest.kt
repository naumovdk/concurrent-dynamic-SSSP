import org.jetbrains.kotlinx.lincheck.LoggingLevel
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.paramgen.DoubleGen
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState
import org.junit.Test


@Param(name = "vertex", gen = IntGen::class, conf = "0:3")
class LinearizabilityTest : VerifierState() {
    private val impl =
        Dijkstra(mutableMapOf(0 to mutableMapOf(), 1 to mutableMapOf(), 2 to mutableMapOf(), 3 to mutableMapOf()), 0)

    @Operation
    public fun setEdge(
        @Param(name = "vertex") from: Int,
        @Param(name = "vertex") to: Int,
        @Param(gen=DoubleGen::class, conf="1:3") newWeight: Double
    ) = impl.setEdge(from, to, newWeight)

    @Operation
    public fun getDistance(@Param(name = "vertex") v: Int) = impl.getDistance(v)

    @Operation
    public fun removeEdge(@Param(name = "vertex") from: Int, @Param(name = "vertex") to: Int) =
        impl.removeEdge(from, to)


    @Test
    fun modelCheckingTest() = StressOptions()
        .threads(3)
        .actorsPerThread(3)
        .iterations(20)
        .sequentialSpecification(Dijkstra::class.java)
        .logLevel(LoggingLevel.INFO)
        .minimizeFailedScenario(true)
        .requireStateEquivalenceImplCheck(false)
        .check(this::class.java)

    override fun extractState(): Any {
        return impl
    }
}
