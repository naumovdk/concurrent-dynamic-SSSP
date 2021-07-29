import junit.framework.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

enum class DssspConstructors(constructor: (Graph, Int) -> Dsssp) {
    Dijkstra(::Dijkstra),
    DssspImpl(::DssspImpl)
}

@RunWith(Parameterized::class)
class SequentialTest {
    @Test
    fun dynamicSmall() {
        val graph: Graph = mutableMapOf(
            1 to mutableMapOf(2 to 1.0, 3 to 3.0),
            2 to mutableMapOf(3 to 1.0, 5 to 4.0),
            3 to mutableMapOf(5 to 1.0, 6 to 7.0),
            5 to mutableMapOf(6 to 8.0),
            6 to mutableMapOf()
        )

        val d = DssspImpl(graph, 1)

        assert(d.getDistance(1) == 0.0)
        assert(d.getDistance(2) == 1.0)
        assert(d.getDistance(3) == 2.0)
        assert(d.getDistance(4) == null)
        assert(d.getDistance(5) == 3.0)
        assert(d.getDistance(6) == 9.0)

        assert(d.removeEdge(5, 6))
        assert(d.removeEdge(3, 6))
        assertFalse(d.removeEdge(5, 5))
        assert(d.getDistance(5) == 3.0)
        assert(d.getDistance(6) == null)

        assert(d.removeEdge(2, 3))
        assert(d.getDistance(3) == 3.0)
        assert(d.getDistance(5) == 4.0)
    }

//    @Test
//    fun addingVertexesAndEdges() {
//        val graph: Graph = mutableMapOf()
//        assert()
//    }
}