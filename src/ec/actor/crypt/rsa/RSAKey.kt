package ec.actor.crypt.rsa

import ec.actor.crypt.Key

interface RSAKey : Key {
    val value: ByteArray
}
