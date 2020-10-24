package ec.actor.crypt.rsa

import ec.actor.crypt.KeyPair
import ec.actor.crypt.KeyPairGenerator
import java.security.KeyPairGenerator as JavaKeyPairGenerator

class RSAKeyPairGenerator(
        blockSize: Int = 128,
        explicitKeySize: Int? = null
) : KeyPairGenerator<RSAPrivateKey, RSAPublicKey> {

    val blockSize = if (blockSize > 22) blockSize else 22
    val keySize: Int = explicitKeySize ?: (this.blockSize + 42) * 8

    private val algorithm = "RSA"
    private val cipher = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding"

    private val backend = JavaKeyPairGenerator.getInstance(algorithm).apply {
        initialize(keySize)
    }

    override operator fun invoke(): KeyPair<RSAPrivateKey, RSAPublicKey> {
        val keyPair = backend.genKeyPair()
        return KeyPair(
                RSAPrivateKey(keyPair.private.encoded, algorithm, cipher),
                RSAPublicKey(keyPair.public.encoded, algorithm, cipher)
        )
    }

    override fun exportPrivate(private: RSAPrivateKey) = private.value

    override fun importPrivate(private: ByteArray) = RSAPrivateKey(private, algorithm, cipher)

    override fun exportPublic(public: RSAPublicKey) = public.value

    override fun importPublic(public: ByteArray) = RSAPublicKey(public, algorithm, cipher)

}
