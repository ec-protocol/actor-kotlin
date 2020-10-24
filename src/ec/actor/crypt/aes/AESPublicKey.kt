package ec.actor.crypt.aes

import ec.actor.crypt.PublicKey
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class AESPublicKey(override val value: ByteArray, algorithm: String, cipher: String) : AESKey, PublicKey {

    private val key = SecretKeySpec(value, algorithm)
    private val cipher = Cipher.getInstance(cipher).apply { init(Cipher.ENCRYPT_MODE, key) }

    override fun encrypt(it: ByteArray): ByteArray = cipher.doFinal(it.run {
        val byteList = toMutableList()
        while (byteList.size % 16 != 0) {
            byteList.add(0)
        }
        byteList.toByteArray()
    })
}