package org.ton.sdk.toncenter.model

import org.ton.sdk.blockchain.address.AddressStd
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmStatic

public inline fun TonCenterMetadataRequest(
    builder: TonCenterMetadataRequest.Builder.() -> Unit
): TonCenterMetadataRequest {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }
    return TonCenterMetadataRequest.Builder().apply(builder).build()
}

public class TonCenterMetadataRequest(
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

        public fun build(): TonCenterMetadataRequest = TonCenterMetadataRequest(
            address = address.toList()
        )
    }

    public companion object {
        @JvmStatic
        public fun builder(): Builder = Builder()
    }
}
