package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable
import org.ton.sdk.blockchain.address.AddressStd
import org.ton.sdk.crypto.HashBytes
import kotlin.contracts.contract
import kotlin.jvm.JvmStatic

public inline fun TonCenterAccountRequest(
    builder: TonCenterAccountRequest.Builder.() -> Unit
): TonCenterAccountRequest {
    contract {
        callsInPlace(builder, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return TonCenterAccountRequest.Builder().apply(builder).build()
}

public class TonCenterAccountRequest(
    public val address: List<AddressStd>,
    public val codeHash: List<HashBytes>,
    public val includeBoc: Boolean
) {
    public class Builder {
        public var address: MutableList<AddressStd> = ArrayList()
        public var codeHash: MutableList<HashBytes> = ArrayList()
        public var includeBoc: Boolean = true

        public fun address(vararg address: AddressStd): Builder = apply {
            this.address.addAll(address)
        }

        public fun address(address: Collection<AddressStd>): Builder = apply {
            this.address.addAll(address)
        }

        public fun includeBoc(includeBoc: Boolean): Builder = apply {
            this.includeBoc = includeBoc
        }

        public fun build(): TonCenterAccountRequest = TonCenterAccountRequest(
            address = address.toList(),
            codeHash = codeHash.toList(),
            includeBoc = includeBoc
        )
    }

    public companion object Companion {
        @JvmStatic
        public fun builder(): Builder = Builder()
    }
}

@Serializable
public class TonCenterAccountStatesResponse(
    public val accounts: List<TonCenterAccountStateFull>,
    public val addressBook: Map<String, TonCenterAddressBookRow>,
    public val metadata: Map<String, TonCenterAddressMetadata>
)
