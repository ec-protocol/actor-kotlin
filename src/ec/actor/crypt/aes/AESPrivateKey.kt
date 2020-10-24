package ec.actor.crypt.aes

import ec.actor.crypt.PrivateKey
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class AESPrivateKey(override val value: ByteArray, algorithm: String, cipher: String) : AESKey, PrivateKey {

    private val key = SecretKeySpec(value, algorithm)
    private val cipher = Cipher.getInstance(cipher).apply { init(Cipher.DECRYPT_MODE, key) }

    override fun decrypt(it: ByteArray): ByteArray = cipher.doFinal(it).run {
        val byteList = toMutableList()
        while (byteList.last() == 0.toByte()) {
            byteList.removeAt(byteList.lastIndex)
        }
        byteList.toByteArray()
    }
}