@file:UseSerializers(ByteStringSerializer::class)

package org.ton.kotlin.adnl

import io.ktor.network.sockets.*
import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.adnl.serializers.ByteStringSerializer
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.TlFixedSize

@Serializable
data class AdnlAddressList(
    val addrs: List<AdnlAddress> = listOf(),
    val version: Int = 0,
    val reinitDate: Int = 0,
    val priority: Int = 0,
    val expireAt: Int = 0,
) : Iterable<AdnlAddress> {
    constructor(vararg addresses: AdnlAddress) : this(addresses.toList())

    override fun iterator(): Iterator<AdnlAddress> = addrs.iterator()
}

@Serializable
sealed interface AdnlAddress {
    fun toInetSocketAddress(): InetSocketAddress

    @Serializable
    @TlConstructorId(0x670da6e7)
    @SerialName("adnl.address.udp")
    data class Udp(val ip: Int, val port: Int) : AdnlAddress {
        constructor(ip: String, port: Int) : this(
            ip.split('.')
                .takeIf { it.size == 4 }
                ?.let { ipv4(it[0].toInt(), it[1].toInt(), it[2].toInt(), it[3].toInt()) }
                ?: throw IllegalArgumentException("Invalid IPv4 address format: `$ip`"),
            port
        )

        val host: String
            get() = buildString(15) {
                append((ip shr 24) and 0xFF)
                append('.')
                append((ip shr 16) and 0xFF)
                append('.')
                append((ip shr 8) and 0xFF)
                append('.')
                append(ip and 0xFF)
            }

        override fun toInetSocketAddress(): InetSocketAddress = InetSocketAddress(host, port)

        override fun toString(): String = "udp://$host:$port"
    }

    @Serializable
    @TlConstructorId(0xe31d63fa)
    @SerialName("adnl.address.udp6")
    data class Udp6(
        @TlFixedSize(16)
        val ip: ByteString,
        val port: Int
    ) : AdnlAddress {
        init {
            require(ip.size == 16) { "IPv6 address must be 16 bytes" }
        }

        val host
            get() = buildString(39) {
                for (i in 0 until 16 step 2) {
                    val value = ((ip[i].toInt() and 0xFF) shl 8) or (ip[i + 1].toInt() and 0xFF)
                    append(value.toString(16))
                    if (i != 14) append(':')
                }
            }

        override fun toInetSocketAddress(): InetSocketAddress = InetSocketAddress(host, port)

        override fun toString(): String = "udp6://[$host]:$port"
    }
}

fun SocketAddress.toAdnlAddress(): AdnlAddress {
    if (this !is InetSocketAddress) {
        throw IllegalArgumentException("Unsupported socket address type: ${this::class.simpleName}")
    }
    val addr = resolveAddress() ?: throw IllegalArgumentException("Cannot resolve address for $this")
    when (addr.size) {
        16 -> {
            return AdnlAddress.Udp6(ByteString(*addr), port)
        }

        4 -> {
            val ip = ipv4(addr[0].toInt(), addr[1].toInt(), addr[2].toInt(), addr[3].toInt())
            return AdnlAddress.Udp(ip, port)
        }

        else -> {
            throw IllegalArgumentException("Unsupported address size: ${addr.size}, expected 4 or 16 bytes")
        }
    }
}

private fun ipv4(a: Int, b: Int, c: Int, d: Int): Int = ((a and 0xFF) shl 24) or
        ((b and 0xFF) shl 16) or
        ((c and 0xFF) shl 8) or
        (d and 0xFF)
