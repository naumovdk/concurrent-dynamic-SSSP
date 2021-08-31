package concurrent

import java.util.*
import java.util.concurrent.atomic.AtomicReference

data class GlobalDescriptor(
    val priority: Long,
    val priorityQueue: PriorityQueue<QueuedVertex> = PriorityQueue(compareBy { it.priority }),
    val status: AtomicReference<Status> = AtomicReference(Status.IN_PROGRESS)
)