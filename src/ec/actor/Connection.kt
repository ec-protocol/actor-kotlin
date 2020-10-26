package ec.actor

import ec.actor.crypt.aes.AESKeyPairGenerator
import ec.actor.crypt.aes.AESPrivateKey
import ec.actor.crypt.aes.AESPublicKey
import ec.actor.crypt.rsa.RSAKeyPairGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    private var inputSessionKey: AESPrivateKey? = null
    private var outputSessionKey: AESPublicKey? = null
    private val controlInput: Channel<Channel<ByteArray?>> = Channel()

    fun init(encrypt: Boolean = true) {
        if (!encrypt) {
            startHandelInput()
            startHandelOutput()
            return
        }

        val initDone = Channel<Unit>()

        val rsaKeyPairGenerator = RSAKeyPairGenerator(explicitKeySize = 2048)
        val keyPair = rsaKeyPairGenerator()
        val escapedEncodedPublicKey = escape(rsaKeyPairGenerator.exportPublic(keyPair.public))

        scope.launch {
            rawOutputChannel.send(
                    byteArrayOf(
                            ControlByte.CONTROL_PACKAGE_START.value,
                            *escapedEncodedPublicKey,
                            ControlByte.CONTROL_PACKAGE_END.value
                    )
            )
        }

        scope.launch {
            var controlPackageChannel = controlInput.receive()
            var controlPackage = byteArrayOf()
            for (controlPackageSection in controlPackageChannel) {
                if (controlPackageSection == null) break
                controlPackage += controlPackageSection
            }
            val therePublicKey = rsaKeyPairGenerator.importPublic(unescape(controlPackage))
            val aesKeyPairGenerator = AESKeyPairGenerator()
            val sessionKey = aesKeyPairGenerator()
            inputSessionKey = sessionKey.private
            val exportedSessionKey = aesKeyPairGenerator.exportPublic(sessionKey.public)
            val encryptedSessionKey = escape(therePublicKey.encrypt(exportedSessionKey))
            rawOutputChannel.send(
                    byteArrayOf(
                            ControlByte.CONTROL_PACKAGE_START.value,
                            *encryptedSessionKey,
                            ControlByte.CONTROL_PACKAGE_END.value
                    )
            )
            controlPackageChannel = controlInput.receive()
            controlPackage = byteArrayOf()
            for (controlPackageSection in controlPackageChannel) {
                if (controlPackageSection == null) break
                controlPackage += controlPackageSection
            }

            outputSessionKey = aesKeyPairGenerator.importPublic(keyPair.decrypt(unescape(controlPackage)))

            resetInput.send(Unit)
            resetOutput.send(Unit)

            initDone.send(Unit)
        }

        startHandelInput()
        startHandelOutput()

        runBlocking { initDone.receive() }
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

    private suspend fun handleInput() {
        val encrypt = inputSessionKey != null && outputSessionKey != null
        var state = 0
        var currentPackageChannel: Channel<ByteArray?>? = null
        var currentControlPackageChannel: Channel<ByteArray?>? = null
        var leftover = byteArrayOf()
        var section = mutableListOf<Byte>()
        var controlSection = mutableListOf<Byte>()
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
                element.forEach {
                    when (it) {
                        ControlByte.PACKAGE_START.value -> {
                            if (state != 0) throw IllegalStateException()
                            state = 1
                            currentPackageChannel = Channel()
                            input.send(currentPackageChannel!!)
                        }
                        ControlByte.PACKAGE_END.value -> {
                            if (state != 1) throw IllegalStateException()
                            state = 0
                            if (section.isNotEmpty()) currentPackageChannel!!.send(section.toByteArray())
                            currentPackageChannel!!.send(null)
                            section = mutableListOf()
                        }
                        ControlByte.CONTROL_PACKAGE_START.value -> {
                            if (state != 0) throw IllegalStateException()
                            state = 2
                            currentControlPackageChannel = Channel()
                            controlInput.send(currentControlPackageChannel!!)
                        }
                        ControlByte.CONTROL_PACKAGE_END.value -> {
                            if (state != 2) throw IllegalStateException()
                            state = 0
                            if (controlSection.isNotEmpty()) currentControlPackageChannel!!.send(controlSection.toByteArray())
                            currentControlPackageChannel!!.send(null)
                            controlSection = mutableListOf()
                        }
                        ControlByte.IGNORE.value -> Unit
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

    private suspend fun handleOutput() {
        val encrypt = inputSessionKey != null && outputSessionKey != null
        whileSelect {
            resetOutput.onReceive {
                startHandelOutput()
                false
            }
            cancelOutput.onReceive {
                false
            }
            output.onReceive {
                var currentChannel: Channel<ByteArray?> = it
                var packageStartSend = false;
                var packageEndSend = false;
                whileSelect {
                    resetOutput.onReceive {
                        startHandelOutput()
                        false
                    }
                    cancelOutput.onReceive {
                        false
                    }
                    it.onReceive {
                        var section = it
                        val controlBytes = ControlByte.values().map { it.value }
                        if (section?.firstOrNull { controlBytes.contains(it) } != null) {
                            throw IllegalStateException(
                                    "byte values ${controlBytes.joinToString()} are used as control characters and " +
                                    "can therefore not be sent or must be encoded or escaped"
                            )
                        }
                        if (section == null) {
                            section = byteArrayOf(ControlByte.PACKAGE_END.value)
                            packageEndSend = true
                        }
                        if (!packageStartSend) {
                            section = byteArrayOf(ControlByte.PACKAGE_START.value) + section
                            packageStartSend = true
                        }
                        //TODO add encryption
                        rawOutputChannel.send(section)
                        !packageEndSend
                    }
                }
                true
            }
        }
    }
}
