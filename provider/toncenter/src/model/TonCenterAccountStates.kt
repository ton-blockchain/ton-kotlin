package org.ton.kotlin.provider.toncenter.model

import kotlinx.serialization.Serializable
import kotlin.contracts.contract
import kotlin.jvm.JvmStatic

public inline fun TonCenterAccountStatesRequest(
    builder: TonCenterAccountStatesRequest.Builder.() -> Unit
): TonCenterAccountStatesRequest {
    contract {
        callsInPlace(builder, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return TonCenterAccountStatesRequest.Builder().apply(builder).build()
}

public data class TonCenterAccountStatesRequest(
    val address: List<String>,
    val includeBoc: Boolean
) {
    public class Builder {
        public var address: MutableList<String> = ArrayList()
        public var includeBoc: Boolean = true

        public fun address(vararg address: String): Builder = apply {
            this.address.addAll(address)
        }

        public fun address(address: Collection<String>): Builder = apply {
            this.address.addAll(address)
        }

        public fun includeBoc(includeBoc: Boolean): Builder = apply {
            this.includeBoc = includeBoc
        }

        public fun build(): TonCenterAccountStatesRequest = TonCenterAccountStatesRequest(
            address = address,
            includeBoc = includeBoc
        )
    }

    public companion object {
        @JvmStatic
        public fun builder(): Builder = Builder()
    }
}

@Serializable
public data class TonCenterAccountStatesResponse(
    val accounts: List<TonCenterAccountStateFull>,
    val addressBook: Map<String, TonCenterAddressBookRow>,
    val metadata: Map<String, TonCenterAddressMetadata>
)
