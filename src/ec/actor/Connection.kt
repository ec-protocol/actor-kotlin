package ec.actor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.whileSelect
import javax.security.auth.kerberos.EncryptionKey

class Connection(
        private val rawInputChannel: Channel<ByteArray>,
        private val rawOutputChannel: Channel<ByteArray>,
        private val scope: CoroutineScope = GlobalScope
) {

    val input: Channel<Channel<ByteArray>> = Channel()
    val output: Channel<Channel<ByteArray>> = Channel()
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
        var currentPackageChannel = Channel<ByteArray>()
        var currentControlPackageChannel = Channel<ByteArray>()
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
                var remaining = leftover + it
                leftover = byteArrayOf()
                //TODO add encryption
                TODO()
                true
            }
        }
    }

    suspend fun handleOutput() {

    }
}
