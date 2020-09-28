@file:JvmName("Main")

import ec.actor.Connection
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val client = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect("localhost", 6542)

        val readChannel = Channel<ByteArray>()
        val writeChannel = Channel<ByteArray>()

        launch {
            val channel = client.openReadChannel()
            while (true) {
                channel.read {
                    it.moveToByteArray()
                }
            }
        }

        launch {

        }

        val connection = Connection(,)


        /*val server = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().bind("localhost", 6542)
        println("Started server at ${server.localAddress}")

        val socket = server.accept()

            launch {
                println("Socket accepted: ${socket.remoteAddress}")

                val input = socket.openReadChannel()
                val output = socket.openWriteChannel(autoFlush = true)

                try {
                    while (true) {
                        val line = input.readUTF8Line(2048)

                        println("${socket.remoteAddress}: $line")
                        output.write("test")
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    socket.close()
                }
            }*/
    }
}
