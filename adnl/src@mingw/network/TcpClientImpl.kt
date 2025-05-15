@file:OptIn(ExperimentalForeignApi::class)

package org.ton.kotlin.adnl.network

import io.ktor.utils.io.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import platform.posix.INVALID_SOCKET
import platform.posix.SOCKET
import platform.posix.SOCKET_ERROR
import platform.posix.send
import platform.windows.*
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
public actual class TcpClientImpl actual constructor(
) : TcpClient, CoroutineScope {
    override val coroutineContext: CoroutineContext = CoroutineName(toString())
    private var socket: SOCKET = INVALID_SOCKET
    override val input: ByteChannel = ByteChannel()
    override val output: ByteChannel = ByteChannel()

    @OptIn(ExperimentalForeignApi::class)
    private val inputJob = launch(newSingleThreadContext("tcp-input"), start = CoroutineStart.LAZY) {
        try {
            val buffer = ByteArray(1024)
            var result: Int
            do {
                result = recv(socket, buffer.refTo(0), buffer.size.convert(), 0)
                check(result >= 0) { "recv failed with error: ${WSAGetLastError()}" }
                input.writeFully(buffer, 0, result.convert())
            } while (result > 0)
            close()
        } catch (e: Throwable) {
            close(e)
        }
    }
    private val outputJob = launch(newSingleThreadContext("tcp-output"), start = CoroutineStart.LAZY) {
        try {
            val buffer = ByteArray(1024)
            var result: Int
            do {
                val size = output.readAvailable(buffer)
                result = send(socket, buffer.refTo(0), size.convert(), 0)
                check(result >= 0) { "send failed with error: ${WSAGetLastError()}" }
            } while (result > 0)
            close()
        } catch (e: Throwable) {
            close(e)
        }
    }

    override suspend fun connect(host: String, port: Int): Unit = memScoped {
        val result = cValuesOf<addrinfo>()
        getaddrinfo(host, port.toString(), null, result).let {
            check(it == 0) {
                "getaddrinfo failed with error: $it"
            }
        }
        var ptr = result.ptr.pointed.pointed
        while (ptr != null) {
            socket = socket(ptr.ai_family, ptr.ai_socktype, ptr.ai_protocol)
            check(socket != INVALID_SOCKET) { "socket failed with error: ${WSAGetLastError()}" }
            if (connect(socket, ptr.ai_addr, ptr.ai_addrlen.toInt()) == SOCKET_ERROR) {
                closesocket(socket)
                ptr = ptr.ai_next?.pointed
            } else {
                break
            }
        }
        check(socket != INVALID_SOCKET) { "Unable to connect to server!" }
        inputJob.start()
        outputJob.start()
    }

    override fun close() {
    }

    override fun close(e: Throwable?) {
        input.close(e)
        output.close(e)
        closesocket(socket)
    }

    public companion object {
        init {
            WSAStartup(0x0202u, null)
        }
    }
}
