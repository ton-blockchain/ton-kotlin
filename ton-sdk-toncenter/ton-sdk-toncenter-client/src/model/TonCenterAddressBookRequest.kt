package org.ton.sdk.toncenter.model

import org.ton.sdk.blockchain.address.AddressStd
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

public inline fun TonCenterAddressBookRequest(
    builder: TonCenterAddressBookRequest.Builder.() -> Unit
): TonCenterAddressBookRequest {
    contract {
        callsInPlace(builder, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return TonCenterAddressBookRequest.Builder().apply(builder).build()
}

public class TonCenterAddressBookRequest(
    @get:JvmName("address")
    public val address: List<AddressStd>
) {
    public class Builder {
        public var address: MutableList<AddressStd> = ArrayList()

        public fun address(vararg address: AddressStd): Builder = apply {
            this.address.addAll(address)
        }

        public fun address(address: Collection<AddressStd>): Builder = apply {
            this.address.addAll(address)
        }

        public fun build(): TonCenterAddressBookRequest = TonCenterAddressBookRequest(
            address = address.toList()
        )
    }

    public companion object {
        @JvmStatic
        public fun builder(): Builder = Builder()
    }
}
