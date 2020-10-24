package ec.actor.crypt

interface PublicKey : Key {
    fun encrypt(it: ByteArray): ByteArray
}
