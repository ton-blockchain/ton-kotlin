package org.ton.kotlin.adnl

import io.ktor.network.sockets.*

class AddressList(
    val addresses: List<Address>
) : Iterable<Address> {
    override fun iterator(): Iterator<Address> = addresses.iterator()
}

sealed interface Address

class UdpAddress(
    val socketAddress: SocketAddress
) : Address
