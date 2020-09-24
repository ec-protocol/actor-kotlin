@file:JvmName("Main")

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.util.cio.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect("localhost", 6542)

        val server = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().bind("localhost", 6542)
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
            }
    }
}
