package ec.actor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.whileSelect

class Connection(
        private val rawInputChannel: Channel<ByteArray>,
        private val rawOutputChannel: Channel<ByteArray>,
        private val scope: CoroutineScope = GlobalScope
) {

    val input: Channel<Channel<ByteArray?>> = Channel()
    val output: Channel<Channel<ByteArray?>> = Channel()
    private val resetInput: Channel<Unit> = Channel()
    private val resetOutput: Channel<Unit> = Channel()
    private val cancelInput: Channel<Unit> = Channel()
    private val cancelOutput: Channel<Unit> = Channel()
    private val inputSessionKey: ByteArray = ByteArray(0)
    private val outputSessionKey: ByteArray = ByteArray(0)
    private val controlInput: Channel<ByteArray> = Channel()

    fun init(encrypt: Boolean = true) {
        if (!encrypt) {
            startHandelInput()
            startHandelOutput()
        }
    }

    private fun startHandelInput() {
        scope.launch {
            handleInput()
        }
    }

    private fun startHandelOutput() {
        scope.launch {
            handleOutput()
        }
    }

    suspend fun handleInput() {
        val encrypt = inputSessionKey.isNotEmpty() && outputSessionKey.isNotEmpty()
        var state = 0
        var currentPackageChannel: Channel<ByteArray?>? = null
        var currentControlPackageChannel: Channel<ByteArray?>? = null
        var leftover = byteArrayOf()
        whileSelect {
            resetInput.onReceive {
                startHandelInput()
                false
            }
            cancelInput.onReceive {
                false
            }
            rawInputChannel.onReceive {
                var element = leftover + it
                leftover = byteArrayOf()
                //TODO add encryption
                var section = mutableListOf<Byte>()
                var controlSection = mutableListOf<Byte>()
                element.forEach {
                    when (it) {
                        ControlByte.PACKAGE_START.value -> {
                            if (state != 0) throw IllegalStateException()
                            state = 1
                            currentPackageChannel = Channel()
                            input.send(currentPackageChannel!!)
                        }
                        ControlByte.PACKAGE_END.value -> {
                            if (state != 0) throw IllegalStateException()
                            state = 0
                            if (section.isNotEmpty()) currentPackageChannel!!.send(section.toByteArray())
                            currentPackageChannel!!.send(null)
                            section = mutableListOf()
                        }
                        ControlByte.CONTROL_PACKAGE_START.value -> {
                            if (state != 0) throw IllegalStateException()
                            state = 2
                            currentControlPackageChannel = Channel()
                            input.send(currentControlPackageChannel!!)
                        }
                        ControlByte.CONTROL_PACKAGE_END.value -> {
                            if (state != 0) throw IllegalStateException()
                            state = 0
                            if (controlSection.isNotEmpty()) currentControlPackageChannel!!.send(controlSection.toByteArray())
                            currentControlPackageChannel!!.send(null)
                            controlSection = mutableListOf()
                        }
                        ControlByte.IGNORE.value -> {
                        }
                        else                                    -> {
                            when (state) {
                                1 -> {
                                    section.add(it)
                                }
                                2 -> {
                                    controlSection.add(it)
                                }
                            }
                        }
                    }
                }
                true
            }
        }
    }

    suspend fun handleOutput() {

    }
}
