
import concurrent.ConcurrentDsssp
import org.junit.jupiter.api.Test

class SequentialTest {
    @Test
    fun l5() {
        val d = ConcurrentDsssp()
        assert(d.setEdge(4, 1, 11.0))
        assert(d.setEdge(0, 3, 17.0))
        assert(d.setEdge(3, 4, 35.0))
        assert(d.setEdge(1, 2, 37.0))
        assert(d.getDistance(2) == 11.0 + 17.0 + 35.0 + 37.0)
    }

    @Test
    fun l6() {
        val d = ConcurrentDsssp()
        assert(d.setEdge(1, 0, 1.0))
        assert(d.getDistance(0) == 0.0)
    }

    @Test
    fun l7() {
        val d = ConcurrentDsssp()
        val r = d.setEdge(5, 3, 3.0)
        assert(r)
        assert(d.setEdge(4, 1, 3.0))
        assert(d.setEdge(3, 4, 9.0))
        assert(d.setEdge(3, 1, 39.0))
        d.setEdge(0, 5, 27.0)
    }

    @Test
    fun l8() {
        val d = ConcurrentDsssp()
        d.setEdge(2,4,21.0)
        d.setEdge(4,2,31.0)
        d.setEdge(0,2,39.0)
        assert(d.getDistance(4) == 60.0)
    }

    @Test
    fun l9() {
        val d = ConcurrentDsssp()
        d.setEdge(0, 2, 1.0)
        assert(d.getDistance(2) == 1.0)
    }

    @Test
    fun l10() {
        val d = ConcurrentDsssp()
        d.setEdge(1,5,21.0)
        d.setEdge(1,5,31.0)
        d.setEdge(0,1,13.0)
        assert(d.getDistance(5) == 13.0 + 31.0)
    }

    @Test
    fun l11() {
        val d = ConcurrentDsssp()
        d.setEdge(4,3,11.0)
        d.setEdge(5,2,13.0)
        d.setEdge(3,2,39.0)
        d.setEdge(3,5,1.0)
        d.setEdge(0,4,5.0)
        assert(d.getDistance(3) == 16.0)
        assert(d.getDistance(2) == 5.0 + 11.0 + 1.0 + 13.0)
    }

    @Test
    fun l12() {
        val d = ConcurrentDsssp()
        d.setEdge(0,2, 3.0)
        assert(d.getDistance(0) == 0.0)
    }

    @Test
    fun l13() {
        val d = ConcurrentDsssp()
        d.setEdge(0,7,15.0 + 27.0)
//        d.setEdge(4, 7, 27.0)
        d.setEdge(6, 3, 9.0)
        d.setEdge(3, 2, 15.0)
        d.setEdge(7, 6, 31.0)
        d.setEdge(1, 3, 1.0)
        assert(d.getDistance(2) == 15.0 + 27.0 + 9.0 + 15.0 + 31.0)
    }

}