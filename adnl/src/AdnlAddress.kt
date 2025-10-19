@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.kotlin.adnl

import io.ktor.network.sockets.*
import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.TlFixedSize
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
public class AdnlAddressList(
    public val addrs: List<AdnlAddress> = listOf(),
    public val version: Int = 0,
    public val reinitDate: Int = 0,
    public val priority: Int = 0,
    public val expireAt: Int = 0,
) : Iterable<AdnlAddress> {
    public constructor(vararg addresses: AdnlAddress) : this(addresses.toList())

    override fun iterator(): Iterator<AdnlAddress> = addrs.iterator()

    override fun toString(): String = addrs.joinToString(separator = ", ", prefix = "[", postfix = "]")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AdnlAddressList) return false
        if (version != other.version) return false
        if (reinitDate != other.reinitDate) return false
        if (priority != other.priority) return false
        if (expireAt != other.expireAt) return false
        if (addrs != other.addrs) return false
        return true
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + reinitDate
        result = 31 * result + priority
        result = 31 * result + expireAt
        result = 31 * result + addrs.hashCode()
        return result
    }
}

@Serializable
public sealed interface AdnlAddress {
    public fun toInetSocketAddress(): InetSocketAddress

    @Serializable
    @TlConstructorId(0x670da6e7)
    @SerialName("adnl.address.udp")
    public class Udp(public val ip: Int, public val port: Int) : AdnlAddress {
        public constructor(ip: String, port: Int) : this(
            ip.split('.')
                .takeIf { it.size == 4 }
                ?.let { ipv4(it[0].toInt(), it[1].toInt(), it[2].toInt(), it[3].toInt()) }
                ?: throw IllegalArgumentException("Invalid IPv4 address format: `$ip`"),
            port
        )

        public val host: String
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

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Udp) return false
            if (ip != other.ip) return false
            if (port != other.port) return false
            return true
        }

        override fun hashCode(): Int {
            var result = ip
            result = 31 * result + port
            return result
        }
    }

    @Serializable
    @TlConstructorId(0xe31d63fa)
    @SerialName("adnl.address.udp6")
    public class Udp6(
        @TlFixedSize(16)
        public val ip: ByteString,
        public val port: Int
    ) : AdnlAddress {
        init {
            require(ip.size == 16) { "IPv6 address must be 16 bytes" }
        }

        public val host: String
            get() = buildString(39) {
                for (i in 0 until 16 step 2) {
                    val value = ((ip[i].toInt() and 0xFF) shl 8) or (ip[i + 1].toInt() and 0xFF)
                    append(value.toString(16))
                    if (i != 14) append(':')
                }
            }

        override fun toInetSocketAddress(): InetSocketAddress = InetSocketAddress(host, port)

        override fun toString(): String = "udp6://[$host]:$port"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Udp6) return false
            if (port != other.port) return false
            if (ip != other.ip) return false
            return true
        }

        override fun hashCode(): Int {
            var result = port
            result = 31 * result + ip.hashCode()
            return result
        }
    }
}

public fun SocketAddress.toAdnlAddress(): AdnlAddress {
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
