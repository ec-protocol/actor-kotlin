@file:JvmName("Main")

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import ec.actor.Connection
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class Main : CliktCommand() {

    val unsafe: Boolean by option(help = "disables encryption").flag("u", default = false)
    val connect: String? by option("c", help = "address to connect to")
    val listen: String? by option("l", help = "address to listen on")
    val input: String? by option("in", "i", help = "input file path")
    val output: String? by option("out", "o", help = "out file path")

    override fun run() = runBlocking {
        var isClient = true
        val socket = try {
            aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect(
                    (connect ?: "").substringBefore(":"),
                    (connect?.substringAfter(":")?.toInt())!!
            )
        } catch (e: Exception) {
            isClient = false
            aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().bind(
                    (listen ?: "").substringBefore(":"),
                    (listen?.substringAfter(":")?.toInt())!!
            ).accept()
        }

        val readChannel = Channel<ByteArray>()
        val writeChannel = Channel<ByteArray>()

        launch {
            val channel = socket.openReadChannel()
            while (true) {
                var buffer = byteArrayOf()
                channel.read {
                    buffer = ByteArray(it.remaining())
                    it.get(buffer)
                }
                readChannel.send(buffer)
            }
        }

        launch {
            val channel = socket.openWriteChannel(true)
            while (true) {
                val buf = writeChannel.receive()
                channel.writeFully(buf, 0, buf.size)
            }
        }

        val connection = Connection(readChannel, writeChannel)
        connection.init(!unsafe)

        if (isClient) {
            val inBytes = File(input ?: "").readBytes()
            val pkgInputChannel = connection.input.receive()
            var pkg = byteArrayOf()
            while (true) {
                val it = pkgInputChannel.receive() ?: break
                pkg += it
            }
            val received = unescape(pkg)
            File(output ?: "").apply {
                if (exists()) return@apply
                parentFile?.mkdirs()
                createNewFile()
            }.writeBytes(received)
            if (inBytes contentEquals received) {
                println("complete")
            } else {
                println("failed")
            }
        } else {
            val channel = Channel<ByteArray?>()
            connection.output.send(channel)
            val element = escape(File(input ?: "").readBytes())
            channel.send(element)
            channel.send(null)
        }
    }
}

fun main(args: Array<String>) = Main().main(args)
