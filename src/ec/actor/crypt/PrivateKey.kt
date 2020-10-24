package ec.actor.crypt

interface PrivateKey : Key {
    fun decrypt(it: ByteArray): ByteArray
}
