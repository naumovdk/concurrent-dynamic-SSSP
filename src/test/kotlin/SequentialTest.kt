import junit.framework.Assert.assertFalse
import org.junit.Test

class SequentialTest {
    @Test
    fun onlySuccessfulDecrements() {
        val d = DssspImpl(0)
        assert(d.setEdge(0, 1, 1.0))
        assert(d.getDistance(1) == 1.0)
        assert(d.setEdge(0, 2, 0.0))
        assert(d.setEdge(2, 1, 0.0))
        assert(d.getDistance(2) == 0.0)
        assert(d.getDistance(1) == 0.0)
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
    fun cycleTest() {
        val d = DssspImpl(0)
        d.setEdge(0, 3, 37.0)
        d.setEdge(3, 1, 31.0)
        d.setEdge(1, 2, 27.0)
        d.setEdge(0, 2, 31.0)
        d.setEdge(2, 1, 23.0)
    }

    @Test
    fun doubleSuccessfulRelaxationTest() {
        val d = DssspImpl(0)
        d.setEdge(0, 1, 37.0)
        d.setEdge(1, 2, 5.0)
        d.setEdge(1, 4, 9.0)
        d.setEdge(2, 4, 3.0)
        d.setEdge(0, 1, 33.0)
        assert(d.getDistance(4) == 41.0)
    }


}