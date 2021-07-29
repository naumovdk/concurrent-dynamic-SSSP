import org.jetbrains.kotlinx.lincheck.annotations.Operation

class LinearizabilityTest {
    private val graph: Graph = mutableMapOf(0 to mutableMapOf())
    private val impl = DssspImpl(graph, 0)

    @Operation
    public
}