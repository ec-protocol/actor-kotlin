package ec.actor.crypt.rsa

import ec.actor.crypt.PublicKey
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class RSAPublicKey(override val value: ByteArray, algorithm: String, cipher: String) : RSAKey, PublicKey {

    private val keySpec = X509EncodedKeySpec(value)
    private val keyFactory = KeyFactory.getInstance(algorithm)
    private val key = keyFactory.generatePublic(keySpec)
    private val cipher = Cipher.getInstance(cipher).apply { init(Cipher.ENCRYPT_MODE, key) }

    override fun encrypt(it: ByteArray): ByteArray = cipher.doFinal(it)
}
