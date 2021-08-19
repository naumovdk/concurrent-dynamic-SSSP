import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

class SequentialTest {
    @Test
    fun addingVertexesAndEdges() {
        val d = DssspImpl(1)
        assert(d.addVertex(2))
        assert(d.addVertex(3))
        assert(d.addVertex(5))
        assert(d.addVertex(6))

        assert(d.setEdge(1, 2, 1.0))
        assert(d.setEdge(1, 3, 3.0))
        assert(d.setEdge(2, 3, 1.0))
        assert(d.setEdge(2, 5, 5.0))
        assert(d.setEdge(3, 5, 1.0))
        assert(d.setEdge(3, 6, 7.0))
        assertFalse(d.setEdge(5, 6, 8.0))

        assert(d.getDistance(1) == 0.0)
        assert(d.getDistance(2) == 1.0)
        assert(d.getDistance(3) == 2.0)
        assert(d.getDistance(4) == null)
        assert(d.getDistance(5) == 3.0)
        assert(d.getDistance(6) == 9.0)
    }

    @Test
    fun onlySuccessfulDecrements() {
        val d = DssspImpl(0)
        assert(d.getDistance(2) == null)
        assert(d.addVertex(1))
        assert(d.setEdge(0, 1, 1.0))
        assert(d.getDistance(1) == 1.0)
        assert(d.addVertex(2))
        assert(d.setEdge(0, 2, 0.0))
        assert(d.setEdge(2, 1, 0.0))
        assert(d.getDistance(2) == 0.0)
        assert(d.getDistance(1) == 0.0)
        assert(d.addVertex(3))
        assert(d.addVertex(4))
        assertFalse(d.addVertex(3))
        assert(d.setEdge(1, 3, 7.0))
        assert(d.setEdge(3, 4, 7.0))
        assert(d.getDistance(3) == 7.0)
        assert(d.getDistance(4) == 14.0)
        assert(d.setEdge(2, 3, 3.0))
        assert(d.getDistance(3) == 3.0)
        assert(d.getDistance(4) == 10.0)
        assertFalse(d.setEdge(0, 1, 7.0))
    }

    @Test
    fun lincheck3() {
        val d = DssspImpl(0)

        d.setEdge(2, 1, 27.0)
        d.setEdge(1, 0, 5.0)
        d.setEdge(3, 3, 31.0)
        d.getDistance(3)
        d.addVertex(2)

        d.getDistance(1)
        d.addVertex(3)
        d.setEdge(2, 3, 9.0)
        d.setEdge(2, 2, 9.0)
        d.setEdge(3, 0, 31.0)
        d.setEdge(3, 1, 3.0)
    }

    @Test
    fun lincheck4() {
        val d = Dijkstra()
        d.setEdge(2, 2, 1.0)
        d.setEdge(3, 3, 7.0)
        d.setEdge(0, 2, 17.0)
        d.addVertex(0)
        d.setEdge(0, 3, 9.0)
        d.getDistance(1)
        d.addVertex(2)
        d.setEdge(2, 2, 15.0)
        d.getDistance(0)
        d.setEdge(0, 1, 25.0)
        d.setEdge(1, 1, 1.0)
    }

    @Test
    fun lincheck5() {
        val d = Dijkstra()
        assert(d.addVertex(2))
        assert(d.getDistance(2) == Double.POSITIVE_INFINITY)
        assert(d.addVertex(3))
    }
}