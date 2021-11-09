package concurrent.process

enum class Status {
    INIT,
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

    fun isTerminated() = !isInProgress()

    fun next() = when (this) {
        INIT -> SCAN
        SCAN -> RELAXATION
        RELAXATION -> UPDATE_DISTANCES
        UPDATE_DISTANCES -> SUCCESS

        ABORTED -> ABORTED
        SUCCESS -> SUCCESS
    }
}
