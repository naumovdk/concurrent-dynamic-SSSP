package concurrent

import concurrent.Status.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

class Vertex(val index: Int, distance: Double = Double.POSITIVE_INFINITY) {
    val outgoing = ConcurrentHashMap<Int, Double>()
    val local: AtomicReference<LocalDescriptor> = AtomicReference(
        LocalDescriptor(
            Distance(distance, null),
            Distance(Double.POSITIVE_INFINITY, null),
            GlobalDescriptor(-1, PriorityQueue(), AtomicReference(SUCCESS))
        )
    )

    fun acquireIfImproves(newDist: Distance, global: GlobalDescriptor): Boolean? {
        while (true) {
            if (global.status.get() == ABORTED) {
                return null
            }
            val cur = local.get()
            val curStatus = cur.global.status.get()
            if (curStatus != IN_PROGRESS || global == cur.global) {
                val curDist = if (global === cur.global) cur.newDist else cur.readDistance(curStatus)
                if (curDist <= newDist) {
                    return false
                }
                val new = LocalDescriptor(newDist, curDist, global)
                if (local.compareAndSet(cur, new)) {
                    return true
                }
            } else {
                when {
                    global.priority > cur.global.priority -> {
                        cur.global.status.compareAndSet(IN_PROGRESS, ABORTED)
                    }
                    global.priority < cur.global.priority -> {
                        global.status.compareAndSet(IN_PROGRESS, ABORTED)
                        return false
                    }
                    else -> {
                        throw Exception("impossible, helping should be there")
                    }
                }
            }
        }
    }

    fun readDistance(): Distance {
        while (true) {
            val cur = local.get()
            val status = cur.global.status.get()
            if (cur === local.get()) {
                return cur.readDistance(status)
            }
        }
    }
}