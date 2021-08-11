import junit.framework.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized


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

        val d = Dijkstra(graph, 1)

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

    @Test
    fun addingVertexesAndEdges() {
        val d = Dijkstra()
        d.addVertex(0)
        d.addVertex(1)
        d.setEdge(0, 1, 1.0)
        assert(d.getDistance(1) == 1.0)
        d.addVertex(2)
        d.setEdge(1, 2, 14.0)
        assert(d.getDistance(2) == 15.0)
        d.addVertex(3)
        d.setEdge(1, 3, 5.0)
        d.setEdge(3, 2, 20.0)
        assert(d.getDistance(2) == 15.0)
        d.removeEdge(1, 2)
        assert(d.getDistance(2) == 26.0)
        d.removeEdge(0, 1)
        assert(d.getDistance(2) == null)
    }

//    @Test
//    fun t1() {
//        val d = Dijkstra()
//        assert(d.getDistance(2) == null)
//        d.setEdge(1, 2, )
//    }
}