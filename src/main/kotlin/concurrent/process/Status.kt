package concurrent.process

enum class Status {
    SUCCESS, ACQUIRE_FROM, ACQUIRE_TO, PLANT_EDGE, SCAN, RELAXATION, ABORTED;

    fun isInProgress(): Boolean {
        return this != SUCCESS || this != ABORTED
    }

    fun isFinished(): Boolean {
        return !isInProgress()
    }

    fun isNotInProgress(): Boolean {
        return !isInProgress()
    }
}
