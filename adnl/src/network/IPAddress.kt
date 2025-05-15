package org.ton.kotlin.adnl.network

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.adnl.adnl.AdnlAddress
import org.ton.kotlin.adnl.adnl.AdnlAddressUdp
import org.ton.kotlin.adnl.adnl.AdnlAddressUdp6
import org.ton.kotlin.adnl.ipv4
import kotlin.jvm.JvmStatic

public sealed interface IPAddress {
    public val host: String
    public val port: Int

    public fun toAdnlAddress(): AdnlAddress

    public companion object {
        @JvmStatic
        public fun ipv4(host: String, port: Int): IPAddress = IPv4Address(host, port)

        @JvmStatic
        public fun ipv4(address: Int, port: Int): IPAddress = IPv4Address(address, port)

        @JvmStatic
        public fun ipv6(address: ByteArray, port: Int): IPAddress = IPv6Address(address, port)
    }
}

public data class IPv4Address(
    val address: Int,
    override val port: Int,
) : IPAddress {
    public constructor(host: String, port: Int) : this(ipv4(host), port)

    init {
        require(port in 0..0xFFFF) { "Invalid port: $port" }
    }

    override val host: String
        get() = ipv4(address)

    override fun toAdnlAddress(): AdnlAddressUdp = AdnlAddressUdp(address, port)

    override fun toString(): String = "$host:$port"
}

public data class IPv6Address(
    val address: ByteArray,
    override val port: Int,
) : IPAddress {
    init {
        require(port in 0..0xFFFF) { "Invalid port: $port" }
        require(address.size == 16) { "Invalid address: ${address.contentToString()}" }
    }

    override val host: String
        get() = TODO()

    override fun toAdnlAddress(): AdnlAddressUdp6 = AdnlAddressUdp6(ByteString(address), port)

    override fun toString(): String = "$host:$port"
}
