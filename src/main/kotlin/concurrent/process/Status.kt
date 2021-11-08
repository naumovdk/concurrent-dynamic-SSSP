package concurrent.process

enum class Status {
    ACQUIRE_FROM,
    ACQUIRE_TO,
    UPDATE_OUTGOING,
    UPDATE_INCOMING,
    SCAN,
    RELAXATION,
    UPDATE_DISTANCES,

    ABORTED,
    SUCCESS;

    fun isInProgress() = when (this) {
        SUCCESS -> false
        ABORTED -> false
        else -> true
    }

    fun isNotInProgress() = !isInProgress()

    fun next() = when (this) {
        ACQUIRE_FROM -> ACQUIRE_TO
        ACQUIRE_TO -> UPDATE_OUTGOING
        UPDATE_OUTGOING -> UPDATE_INCOMING
        UPDATE_INCOMING -> SCAN
        SCAN -> RELAXATION
        RELAXATION -> UPDATE_DISTANCES
        UPDATE_DISTANCES -> SUCCESS

        ABORTED -> ABORTED
        SUCCESS -> SUCCESS
    }
}
