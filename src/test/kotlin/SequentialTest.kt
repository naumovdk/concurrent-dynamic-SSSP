import bapi.Panigraham
import concurrent.BasicConcurrentDsssp
import org.junit.jupiter.api.Test
import sequential.DijkstraRecomputing
import sequential.SequentialDsssp

class SequentialTest {
    @Test
    fun l5() {
        val d = BasicConcurrentDsssp()
        assert(d.setEdge(4, 1, 11.0))
        assert(d.setEdge(0, 3, 17.0))
        assert(d.setEdge(3, 4, 35.0))
        assert(d.setEdge(1, 2, 37.0))
        assert(d.getDistance(2) == 11.0 + 17.0 + 35.0 + 37.0)
    }

    @Test
    fun l6() {
        val d = BasicConcurrentDsssp()
        assert(d.setEdge(1, 0, 1.0))
        assert(d.getDistance(0) == 0.0)
    }

    @Test
    fun l7() {
        val d = BasicConcurrentDsssp()
        val r = d.setEdge(5, 3, 3.0)
        assert(r)
        assert(d.setEdge(4, 1, 3.0))
        assert(d.setEdge(3, 4, 9.0))
        assert(d.setEdge(3, 1, 39.0))
        d.setEdge(0, 5, 27.0)
    }

    @Test
    fun l8() {
        val d = BasicConcurrentDsssp()
        d.setEdge(2,4,21.0)
        d.setEdge(4,2,31.0)
        d.setEdge(0,2,39.0)
        assert(d.getDistance(4) == 60.0)
    }

    @Test
    fun l9() {
        val d = BasicConcurrentDsssp()
        d.setEdge(0, 2, 1.0)
        assert(d.getDistance(2) == 1.0)
    }

    @Test
    fun l10() {
        val d = SequentialDsssp()
        d.setEdge(1,5,21.0)
        d.setEdge(1,5,31.0)
        d.setEdge(0,1,13.0)
        assert(d.getDistance(5) == 13.0 + 31.0)
    }

    @Test
    fun l11() {
        val d = BasicConcurrentDsssp()
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
        val d = SequentialDsssp()
        d.setEdge(0,2, 3.0)
        assert(d.getDistance(0) == 0.0)
        assert(d.getDistance(2) == 3.0)
        assert(d.getDistance(3) == Dsssp.INF)
    }

    @Test
    fun l13() {
        val d = BasicConcurrentDsssp()
        d.setEdge(0,7,15.0 + 27.0)
//        d.setEdge(4, 7, 27.0)
        d.setEdge(6, 3, 9.0)
        d.setEdge(3, 2, 15.0)
        d.setEdge(7, 6, 31.0)
        d.setEdge(1, 3, 1.0)
        assert(d.getDistance(2) == 15.0 + 27.0 + 9.0 + 15.0 + 31.0)
    }

    @Test
    fun hang() {
        val d = BasicConcurrentDsssp()
        d.setEdge(1, 0, 37.0)
        d.setEdge(2, 1, 37.0)
        d.setEdge(1, 2, 37.0)
        d.setEdge(0, 1, 25.0)
    }

    @Test
    fun s() {
        val d = Panigraham()
        d.setEdge(2, 3, 1.0)
        d.setEdge(0, 2, 17.0)
        assert(d.getDistance(2) == 17.0)
        d.setEdge(1, 2, 19.0)
        d.setEdge(0, 2, 15.0)
//        assert(ge)
    }

    @Test
    fun bruh() {
        val d = SequentialDsssp()
        d.setEdge(3, 0, 21.0)
        d.setEdge(4, 3, 17.0)
        d.setEdge(5, 3, 27.0)
        assert(d.getDistance(4) == Dsssp.INF)
        d.setEdge(0, 4, 19.0)
        assert(d.getDistance(3) == 19.0 + 17.0)
        assert(d.getDistance(1) == Dsssp.INF)
        d.setEdge(0, 4, 11.0)
        d.setEdge(5, 5, 3.0)
    }

    @Test
    fun bruh1() {
        val d = SequentialDsssp()
        d.setEdge(3, 4, 35.0)
        d.setEdge(2, 3, 19.0)
        d.setEdge(5, 4, 7.0)
        d.setEdge(0, 2, 31.0)
        d.setEdge(4, 3, 5.0)
        d.setEdge(0, 5, 13.0)
        d.setEdge(4, 3, 31.0)
        assert(d.getDistance(4) == 13.0 + 7.0)
        assert(d.getDistance(2) == 31.0)
    }


    @Test
    fun bruh12() {
        val d = SequentialDsssp()
        d.setEdge(0, 6, 25.0)
        d.setEdge(0, 3, 23.0)
        d.setEdge(3, 6, 5.0)
        d.setEdge(0, 6, 37.0)
        d.setEdge(6, 4, 5.0)
        assert(d.getDistance(4) == 23.0 + 5.0 + 5.0)
        assert(d.getDistance(1) == Dsssp.INF)
    }


    @Test
    fun bruh13() {
        val d = DijkstraRecomputing()
        d.setEdge(0, 10, 1.0)
        d.setEdge(11, 3, 15.0)
        d.setEdge(0, 11, 17.0)
        d.setEdge(4, 11, 3.0)
        d.setEdge(3, 6, 1.0)
        d.setEdge(0, 4, 29.0)
        d.setEdge(10, 4, 5.0)
        print(d.getDistance(6))
        assert(d.getDistance(6) == 25.0)
    }

    @Test
    fun bruh14() {
        val d = BasicConcurrentDsssp()
        d.setEdge(0, 4, 25.0)
        d.setEdge(4, 3, 39.0)
        d.setEdge(0, 4, 29.0)
        assert(d.getDistance(3) == 25.0 + 39.0)
    }

    @Test
    fun bruh2() {
        val d = BasicConcurrentDsssp()
        d.setEdge(5, 1, 37.0)
        d.setEdge(0, 1, 37.0)
        d.setEdge(6, 5, 5.0)
        assert(d.getDistance(1) == 37.0)
    }

    @Test
    fun bruh3() {
        val d = BasicConcurrentDsssp()
        d.setEdge(0, 3, 37.0)
        d.setEdge(3, 1, 37.0)
        d.setEdge(1, 6, 5.0)
        assert(d.getDistance(6) == 37.0 + 37.0 + 5.0)
    }

    @Test
    fun bruh4() {
        val d = BasicConcurrentDsssp()
        d.setEdge(0, 3, 37.0)
        d.setEdge(3, 6, 5.0)
    }

    @Test
    fun bruh5() {
        val d = BasicConcurrentDsssp()
        d.setEdge(1, 3, 9.0)
        d.setEdge(2, 1, 1.0)
        d.setEdge(2, 3, 25.0)
        d.setEdge(0, 2, 11.0)
        d.setEdge(1, 3, 35.0)
        assert(d.getDistance(3) == 36.0)
    }

    @Test
    fun bruh6() {
        val d = BasicConcurrentDsssp()
        d.setEdge(2, 0, 35.0)
        d.setEdge(3, 2, 23.0)
        d.setEdge(0, 3, 37.0)
        d.setEdge(3, 2, 37.0)
        d.setEdge(0, 4, 37.0)
        assert(d.getDistance(4) == 37.0)
    }

    @Test
    fun bruh7() {
        val d = BasicConcurrentDsssp()
        d.setEdge(0, 3, 9.0)
        d.setEdge(3, 2, 1.0)
        d.setEdge(0, 3, 35.0)
        assert(d.getDistance(3) == 35.0)
    }

    @Test
    fun bruh8() {
        val d = BasicConcurrentDsssp()
        d.setEdge(1, 0, 1.0)
        d.setEdge(0, 2, 3.0)
        d.setEdge(4, 1, 33.0)
        d.setEdge(2, 4, 21.0)
        d.setEdge(0, 2, 15.0)
        assert(d.getDistance(2) == 15.0)
    }

    @Test
    fun bruh9() {
        val d = BasicConcurrentDsssp()
        d.setEdge(1, 3, 35.0)
        d.setEdge(2, 1, 13.0)
        d.setEdge(0, 2, 13.0)
        d.setEdge(2, 1, 19.0)
        assert(d.getDistance(1) == 32.0)
        assert(d.getDistance(3) == 67.0)
    }

    @Test
    fun bruh10() {
        val d = BasicConcurrentDsssp()
        d.setEdge(4, 5, 3.0)
        d.setEdge(5, 4, 1.0)
        d.setEdge(0, 3, 13.0)
        d.setEdge(3, 5, 5.0)
        d.setEdge(0, 3, 37.0)
        assert(d.getDistance(4) == 37.0 + 5.0 + 1.0)
    }

}