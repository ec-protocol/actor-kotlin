package ec.actor.crypt.aes

import ec.actor.crypt.KeyPair
import ec.actor.crypt.KeyPairGenerator
import javax.crypto.KeyGenerator

class AESKeyPairGenerator(val keySize: Int = 256) : KeyPairGenerator<AESPrivateKey, AESPublicKey> {

    private val algorithm = "AES"
    private val cipher = "AES/ECB/NoPadding"

    private val backend = KeyGenerator.getInstance(algorithm).apply {
        init(keySize)
    }

    override operator fun invoke(): KeyPair<AESPrivateKey, AESPublicKey> {
        val keyPair = backend.generateKey()
        return KeyPair(
                AESPrivateKey(keyPair.encoded, algorithm, cipher), AESPublicKey(keyPair.encoded, algorithm, cipher)
        )
    }

    override fun exportPrivate(private: AESPrivateKey) = private.value

    override fun importPrivate(private: ByteArray) = AESPrivateKey(private, algorithm, cipher)

    override fun exportPublic(public: AESPublicKey) = public.value

    override fun importPublic(public: ByteArray) = AESPublicKey(public, algorithm, cipher)

}
