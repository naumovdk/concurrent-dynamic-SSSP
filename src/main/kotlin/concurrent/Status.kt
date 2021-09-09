package concurrent

enum class Status {
    SUCCESS, INITIALIZATION, RELAXATION, SCAN, ABORTED;

    fun isInProgress(): Boolean {
        return this == INITIALIZATION || this == RELAXATION || this == SCAN
    }

    fun isNotInProgress(): Boolean {
        return !isInProgress()
    }
}
