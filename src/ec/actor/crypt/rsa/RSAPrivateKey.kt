package ec.actor.crypt.rsa

import ec.actor.crypt.PrivateKey
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher

class RSAPrivateKey(override val value: ByteArray, algorithm: String, cipher: String) : RSAKey, PrivateKey {

    private val keySpec = PKCS8EncodedKeySpec(value)
    private val keyFactory = KeyFactory.getInstance(algorithm)
    private val key = keyFactory!!.generatePrivate(keySpec)
    private val cipher = Cipher.getInstance(cipher).apply { init(Cipher.DECRYPT_MODE, key) }

    override fun decrypt(it: ByteArray): ByteArray = cipher.doFinal(it)
}