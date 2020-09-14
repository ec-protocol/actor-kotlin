package ec.actor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.security.auth.kerberos.EncryptionKey

class Connection(
        private val i: Channel<ByteArray>,
        private val o: Channel<ByteArray>,
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
            scope.launch {
                handleInput()
            }
            scope.launch {
                handleOutput()
            }
        }
    }

    suspend fun handleInput() {

    }

    suspend fun handleOutput() {

    }
}
