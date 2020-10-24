package ec.actor.crypt

open class KeyPair<Private : PrivateKey, Public : PublicKey>(val private: Private, val public: Public) {
    fun encrypt(it: ByteArray): ByteArray = public.encrypt(it)
    fun decrypt(it: ByteArray): ByteArray = private.decrypt(it)
}
