package ec.actor.crypt

interface KeyPairGenerator<Private : PrivateKey, Public : PublicKey> {
    operator fun invoke(): KeyPair<Private, Public>
    fun exportPrivate(private: Private): ByteArray
    fun importPrivate(private: ByteArray): Private
    fun exportPublic(public: Public): ByteArray
    fun importPublic(public: ByteArray): Public
}
