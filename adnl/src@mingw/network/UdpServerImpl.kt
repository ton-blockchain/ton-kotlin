package org.ton.kotlin.adnl.network

import io.ktor.utils.io.core.*
import kotlinx.cinterop.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.io.Source
import kotlinx.io.readByteArray
import platform.posix.*
import platform.windows.AF_INET
import platform.windows.IPPROTO_UDP
import platform.windows.SOCK_DGRAM
import platform.windows.WSAGetLastError
import platform.windows.addrinfo
import platform.windows.bind
import platform.windows.getaddrinfo
import platform.windows.recvfrom
import platform.windows.sockaddr_in6
import platform.windows.socket
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalForeignApi::class)
internal actual class UdpServerImpl actual constructor(
    actual override val coroutineContext: CoroutineContext,
    actual val port: Int,
    callback: UdpServer.Callback
) : UdpServer {
    private val outputChannel = Channel<Pair<IPAddress, Source>>()
    private val inputChannel = Channel<Pair<IPAddress, Source>>()
    private val socket = initSocket()
    private val inputJob = launch(newSingleThreadContext("udp-input")) {
        val addr: sockaddr = nativeHeap.alloc()
        val addrLen: IntVar = nativeHeap.alloc()
        try {
            val buf = ByteArray(1500)
            var result: Int
            do {
                result = recvfrom(socket, buf.refTo(0), buf.size.convert(), 0, addr.ptr, addrLen.ptr)
                check(result >= 0) { "recvfrom failed with error: ${WSAGetLastError()}" }
                val packet = ByteReadPacket(buf, 0, result.convert())
                val ip = if (addr.sa_family.toInt() == AF_INET) {
                    val addr4 = addr.reinterpret<sockaddr_in>()
                    IPAddress.ipv4(addr4.sin_addr.S_un.S_addr.toInt(), addr4.sin_port.toInt())
                } else {
                    val addr6 = addr.reinterpret<sockaddr_in6>()
                    IPAddress.ipv6(addr6.sin6_addr.u.Byte.readBytes(16), addr6.sin6_port.toInt())
                }
                inputChannel.send(ip to packet)
            } while (result > 0)
        } finally {
            nativeHeap.free(addr)
            nativeHeap.free(addrLen)
        }
    }
    private val outputJob = launch(newSingleThreadContext("udp-output")) {
        val addr = nativeHeap.alloc<sockaddr>()
        try {
            for ((ip, packet) in outputChannel) {
                val sizeOf = when (ip) {
                    is IPv4Address -> {
                        addr.sa_family = AF_INET.convert()
                        val addr4 = addr.reinterpret<sockaddr_in>()
                        addr4.sin_addr.S_un.S_addr = ip.address.toUInt()
                        addr4.sin_port = ip.port.toUShort()
                        sizeOf<sockaddr_in>()
                    }

                    is IPv6Address -> {
                        addr.sa_family = AF_INET6.convert()
                        val addr6 = addr.reinterpret<sockaddr_in6>()
                        memcpy(
                            addr6.sin6_addr.u.Byte,
                            ip.address.refTo(0),
                            ip.address.size.convert()
                        )
                        addr6.sin6_port = ip.port.toUShort()
                        sizeOf<sockaddr_in6>()
                    }
                }
                try {
                    val bytes = packet.readByteArray()
                    val result = sendto(
                        socket,
                        bytes.refTo(0),
                        bytes.size,
                        0,
                        addr.ptr,
                        sizeOf.convert()
                    )
                    check(result >= 0) { "sendto failed with error: ${WSAGetLastError()}" }
                } finally {
                }
            }
        } catch (e: Throwable) {
            nativeHeap.free(addr)
            close(e)
        }
    }

    actual override suspend fun send(address: IPAddress, data: Source) {

    }

    private fun initSocket(): SOCKET = memScoped {
        val socket = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)
        check(socket != INVALID_SOCKET) { "socket failed with error: ${WSAGetLastError()}" }
        val addr = cValuesOf<addrinfo>()
        check(getaddrinfo(null, port.toString(), null, addr) == 0) {
            "getaddrinfo failed with error: ${WSAGetLastError()}"
        }
        val sockAddr = addr.ptr.pointed.pointed ?: error("getaddrinfo failed")
        check(bind(socket, sockAddr.ai_addr, sockAddr.ai_addrlen.convert()) != SOCKET_ERROR) {
            "bind failed with error: ${WSAGetLastError()}"
        }
        return socket
    }

    private fun close(e: Throwable? = null) {
        inputChannel.close(e)
        outputChannel.close(e)
        closesocket(socket)
    }
}
