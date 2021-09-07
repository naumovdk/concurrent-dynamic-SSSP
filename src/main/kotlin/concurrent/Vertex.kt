package concurrent

import concurrent.GlobalDescriptor.Companion.FAKE
import concurrent.Status.ABORTED
import concurrent.Status.IN_PROGRESS
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

class Vertex(distance: Double = Double.POSITIVE_INFINITY) : Comparable<Vertex> {
    val outgoing = ConcurrentHashMap<Vertex, Edge>()
    val local: AtomicReference<LocalDescriptor> = AtomicReference(
        LocalDescriptor(oldDistance = Distance.INF, newDistance = Distance(distance, null), global = FAKE)
    )

    fun acquire(newDist: Distance?, global: GlobalDescriptor): Distance? {
        while (true) {
            if (global.status.get() == ABORTED) {
                return null
            }
            val cur = local.get()
            val status = cur.global.status.get()

            if (status != IN_PROGRESS) {
                val oldDist = cur.readDistance(status)
                var acquiringDist = newDist ?: oldDist

                if (acquiringDist > oldDist) {
                    acquiringDist = oldDist
                }

                val new = LocalDescriptor(newDistance = acquiringDist, oldDistance = oldDist, global = global)

                if (cur.global === global) {
                    return cur.oldDistance
                }

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
            if (curStatus != IN_PROGRESS) {
                val curDist = cur.readDistance(curStatus)
                if (curDist <= newDist) {
                    return false
                }
                val new = LocalDescriptor(newDistance = newDist, oldDistance = curDist, global)
                if (local.compareAndSet(cur, new)) {
                    return true
                }
            } else if (global === cur.global) {
                val curDist = cur.newDistance
                if (curDist <= newDist) {
                    return false
                }
                val new = LocalDescriptor(newDistance = newDist, oldDistance = cur.oldDistance, global)
                if (local.compareAndSet(cur, new)) {
                    return true
                }
            } else {
                global.helpOrTurnOff(cur.global)
            }
        }
    }

    fun readDistance(): Distance { // todo forbid reading IN_PROGRESS
        while (true) {
            val cur = local.get()
            val status = cur.global.status.get()
            if (status != IN_PROGRESS) {
                if (cur == local.get()) {
                    return cur.readDistance(status)
                }
            } else {
                cur.global.help()
            }
        }
    }

    override fun compareTo(other: Vertex): Int {
        return compareValues(this.local.get().newDistance, other.local.get().newDistance)
    }
}