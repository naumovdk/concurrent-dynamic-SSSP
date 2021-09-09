package concurrent

sealed class Descriptor(val process: Process)

class Descriptor0(process: Process) : Descriptor(process)

class Descriptor1(process: Process) : Descriptor(process)

class BothDescriptors(val process: Process) {
    private val descriptor0 = Descriptor0(process)
    private val descriptor1 = Descriptor1(process)

    fun new(old: Descriptor): Descriptor {
        return when (old) {
            is Descriptor0 -> descriptor1
            is Descriptor1 -> descriptor0
        }
    }
}