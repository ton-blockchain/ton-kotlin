package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable
import org.ton.sdk.blockchain.address.AddressStd
import org.ton.sdk.crypto.HashBytes
import kotlin.contracts.contract
import kotlin.jvm.JvmName
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
    @get:JvmName("address") public val address: List<AddressStd>,
    @get:JvmName("codeHash") public val codeHash: List<HashBytes>,
    @get:JvmName("includeBoc") public val includeBoc: Boolean
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
    @get:JvmName("accounts")
    public val accounts: List<TonCenterAccountStateFull>,
    @get:JvmName("addressBook")
    public val addressBook: Map<String, TonCenterAddressBookRow>,
    @get:JvmName("metadata")
    public val metadata: Map<String, TonCenterAddressMetadata>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as TonCenterAccountStatesResponse

        if (accounts != other.accounts) return false
        if (addressBook != other.addressBook) return false
        if (metadata != other.metadata) return false

        return true
    }

    override fun hashCode(): Int {
        var result = accounts.hashCode()
        result = 31 * result + addressBook.hashCode()
        result = 31 * result + metadata.hashCode()
        return result
    }

    override fun toString(): String = buildString {
        append("TonCenterAccountStatesResponse(")
        append("accounts=$accounts, ")
        append("addressBook=$addressBook, ")
        append("metadata=$metadata")
        append(")")
    }
}
