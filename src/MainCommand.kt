@file:JvmName("Main")

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
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

private const val MAX_PACKAGE_SIZE = 64 * 1024 - 60 - 1

class MainCommand : CliktCommand() {

    val unsafe: Boolean by option("-u", help = "disables encryption").flag(default = false)
    val connect: String? by option("-c", help = "address to connect to")
    val listen: String? by option("-l", help = "address to listen on")
    val input: String? by option("--in", "-i", help = "input file path")
    val output: String? by option("--out", "-o", help = "out file path")

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
            val pkgInputChannel = connection.input.receive()
            var escape = false
            val outFile = File(output ?: "").apply {
                if (exists()) return@apply
                parentFile?.mkdirs()
                createNewFile()
            }.apply { writeBytes(byteArrayOf()) }
            while (true) {
                val it = pkgInputChannel.receive() ?: break
                outFile.appendBytes(contextAwareUnescape(it, escape).apply { escape = second }.first)
            }
            println("complete")
        } else {
            val channel = Channel<ByteArray?>()
            connection.output.send(channel)
            val input = File(input ?: "").inputStream()
            var leftover = byteArrayOf()
            do {
                leftover += escape(input.readNBytes(MAX_PACKAGE_SIZE))
                while(leftover.size >= MAX_PACKAGE_SIZE) {
                    channel.send(leftover.sliceArray(0 until MAX_PACKAGE_SIZE))
                    leftover = leftover.sliceArray(MAX_PACKAGE_SIZE until leftover.size)
                }
            } while (input.available() > 0)
            if(leftover.isNotEmpty()) {
                channel.send(leftover)
            }
            channel.send(null)
        }
    }
}

fun main(args: Array<String>) = MainCommand().main(args)
