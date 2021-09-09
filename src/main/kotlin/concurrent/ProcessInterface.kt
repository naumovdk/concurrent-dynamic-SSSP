package concurrent

interface ProcessInterface {
    fun getStatus(): Status
    fun onIntersection(priority: Int)
}
