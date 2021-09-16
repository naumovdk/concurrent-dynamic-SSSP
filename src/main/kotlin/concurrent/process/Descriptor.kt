package concurrent.process

sealed class Descriptor(val process: Process) {
    companion object {
        val UNINITIALIZED: Descriptor = Descriptor0(Process.UNINITIALIZED)
    }
}

class Descriptor0(process: Process) : Descriptor(process)

class Descriptor1(process: Process) : Descriptor(process)

class BothDescriptors(val process: Process) {
    private val descriptor0 = Descriptor0(process)
    private val descriptor1 = Descriptor1(process)

    fun new(other: Descriptor, status: Status): Descriptor {
        return when (other) {
            is Descriptor0 -> if (status == Status.SUCCESS) descriptor1 else descriptor0
            is Descriptor1 -> if (status == Status.SUCCESS) descriptor0 else descriptor1
        }
    }
}