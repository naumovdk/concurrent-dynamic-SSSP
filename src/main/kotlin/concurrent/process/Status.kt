package concurrent.process

enum class Status {
    ACQUIRE_FROM,
    UPDATE_FROM_DISTANCE,
    STORE_OFFERED_DISTANCE,
    ACQUIRE_TO,
    UPDATE_TO_DISTANCE,
    READ_EDGE_EXPECT,
    UPDATE_EDGE,
    SCAN,
    RELAXATION,

    ABORTED,
    SUCCESS;

    fun isInProgress(): Boolean {
        return this !== SUCCESS && this !== ABORTED
    }

    fun isFinished(): Boolean {
        return !isInProgress()
    }

    fun isNotInProgress(): Boolean {
        return !isInProgress()
    }
}
