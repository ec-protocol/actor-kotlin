package ec.actor.crypt.aes

import ec.actor.crypt.Key

interface AESKey : Key {
    val value: ByteArray
}
