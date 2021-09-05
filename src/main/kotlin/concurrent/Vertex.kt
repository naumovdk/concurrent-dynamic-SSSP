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

    fun acquire(newDist: Distance?, global: GlobalDescriptor): Distance {
        while (true) {
            val cur = local.get()
            val status = cur.global.status.get()
            if (status != IN_PROGRESS) {
                val oldDist = cur.readDistance(status)
                val new = LocalDescriptor(newDistance = newDist ?: oldDist, oldDistance = oldDist, global = global)
                if (local.compareAndSet(cur, new)) {
                    return oldDist
                }
            } else {
                global.helpOrTurnOff(cur.global)
            }
        }
    }

    fun acquireIfImproves(newDist: Distance, global: GlobalDescriptor): Boolean? {
        while (true) {
            if (global.status.get() == ABORTED) {
                return null
            }
            val cur = local.get()
            val curStatus = cur.global.status.get()
            if (curStatus != IN_PROGRESS || global == cur.global) {
                val curDist = if (global === cur.global) cur.newDistance else cur.readDistance(curStatus)
                if (curDist <= newDist) {
                    return false
                }
                val new = LocalDescriptor(newDist, curDist, global)
                if (local.compareAndSet(cur, new)) {
                    return true
                }
            } else {
                global.helpOrTurnOff(cur.global)
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