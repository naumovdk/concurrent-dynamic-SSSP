package concurrent.process

enum class Status {
    INIT,
    SCAN,
    RELAXATION,

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
        RELAXATION -> SUCCESS

        ABORTED -> ABORTED
        SUCCESS -> SUCCESS
    }
}
