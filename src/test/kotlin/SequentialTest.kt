
import concurrent.ConcurrentDsssp
import concurrent.Process
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SequentialTest {
    @Test
    fun noIncrementalSupportTest() {
        val d = ConcurrentDsssp(0)
        assert(d.getDistance(1) == Double.POSITIVE_INFINITY)
        assert(d.setEdge(1, 2, 3.0))
        assert(d.setEdge(2, 4, 3.0))
        assert(d.setEdge(1, 4, 3.0))
        assert(d.setEdge(4, 5, 10.0))
        assert(d.getDistance(1) == Double.POSITIVE_INFINITY)
        assert(d.getDistance(4) == Double.POSITIVE_INFINITY)
        assert(d.setEdge(0, 2, 4.0))
        print(d.getDistance(5))
        assert(d.getDistance(5) == 17.0)
        assert(d.getDistance(2) == 4.0)
        assert(d.setEdge(0, 1, 2.0))
        assert(d.getDistance(5) == 15.0)
        assert(d.getDistance(2) == 4.0)
        try {
            d.setEdge(4, 5, 555.0)
        } catch (e: IncrementalIsNotSupportedException) {
            assert(d.getDistance(5) == 15.0)
        }
    }

    @Test
    fun cycleTest() {
        for (d in listOf(SequentialDsssp(), ConcurrentDsssp())) {
            assert(d.setEdge(0, 4, 10.0))
            assert(d.setEdge(1, 2, 2.0))
            assert(d.setEdge(2, 3, 2.0))
            assert(d.setEdge(3, 1, 2.0))
            assert(d.setEdge(3, 4, 2.0))
            assert(d.getDistance(4) == 10.0)
            assert(d.setEdge(0, 1, 1.0))
            assert(d.getDistance(4) == 7.0)
        }
    }

    @Test
    fun l1() {
        for (d in listOf(SequentialDsssp(), ConcurrentDsssp())) {
            assert(d.setEdge(0, 3, 23.0))
            assertThrows<IncrementalIsNotSupportedException> { d.setEdge(0, 3, 27.0) }
            assert(d.getDistance(5) == Double.POSITIVE_INFINITY)
        }
    }

    @Test
    fun l2() {
        for (d in listOf(SequentialDsssp(), ConcurrentDsssp())) {
            assert(d.setEdge(5, 6, 3.0))
            assert(d.setEdge(5, 2, 11.0))
            assert(d.setEdge(6, 2, 5.0))
            assert(d.setEdge(0, 5, 29.0))
            assertThrows<IncrementalIsNotSupportedException> { d.setEdge(6, 2, 7.0) }
            assert(d.getDistance(2) == 29.0 + 3.0 + 5.0)
            assert(d.getDistance(4) == Double.POSITIVE_INFINITY)
        }
    }

    @Test
    fun l3() {
        val d = ConcurrentDsssp()
        assert(d.setEdge(6, 5, 1.0))
        assert(d.setEdge(6, 0, 1.0))
        assert(d.getDistance(0) == 0.0)
    }

    @Test
    fun l4() {
        val d = ConcurrentDsssp()
        assert(d.setEdge(0, 1, 1.0))
        assertThrows<IncrementalIsNotSupportedException> { d.setEdge(0, 1, 11.0) }
        assert(d.getDistance(1) == 1.0)
    }

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
        assert(d.setEdge(5, 3, 3.0))
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
    fun m() {
        val x = Process.UNINITIALIZED
    }
}