package concurrent.process

sealed class Descriptor(val process: Process) {
    companion object {
        val UNINITIALIZED: Descriptor = Descriptor0(Process.UNINITIALIZED)
    }
}

class Descriptor0(process: Process) : Descriptor(process)

class Descriptor1(process: Process) : Descriptor(process)