@file:JvmName("Main")

import ec.actor.Connection
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

fun main() = runBlocking {
    var isClient = true
    val socket = try {
        aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect("localhost", 6542)
    } catch (e: Exception) {
        isClient = false
        aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().bind("localhost", 6542).accept()
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
    connection.init(false)

    if (isClient) {
        val inBytes = File("data/in.mp4").readBytes()
        val pkgInputChannel = connection.input.receive()
        var pkg = byteArrayOf()
        while (true) {
            val it = pkgInputChannel.receive() ?: break
            pkg += it
        }
        val recived = unescape(pkg)
        File("data/out.mp4").apply {
            if (exists()) return@apply
            parentFile?.mkdirs()
            createNewFile()
        }.writeBytes(recived)
        if(inBytes contentEquals recived) {
            println("complete")
        } else {
            println("failed")
        }
    } else {
        val channel = Channel<ByteArray?>()
        connection.output.send(channel)
        val element = escape(File("data/in.mp4").readBytes())
        channel.send(element)
        channel.send(null)
    }
}
