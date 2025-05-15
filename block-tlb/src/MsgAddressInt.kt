@file:Suppress("NOTHING_TO_INLINE", "PropertyName")

package org.ton.kotlin.block

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.TlbCombinator
import kotlin.contracts.contract
import kotlin.jvm.JvmStatic

public inline fun MsgAddressInt(address: String): MsgAddressInt = MsgAddressInt.parse(address)

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("@type")

public sealed interface MsgAddressInt : MsgAddress {
    public val workchainId: Int
    public val address: BitString

    public fun toAddrStd(): AddrStd

    public companion object : TlbCodec<MsgAddressInt> by MsgAddressIntTlbCombinator {
        @JvmStatic
        public fun tlbCodec(): TlbCombinator<MsgAddressInt> = MsgAddressIntTlbCombinator

        @JvmStatic
        public fun toString(
            address: MsgAddressInt,
            userFriendly: Boolean = true,
            urlSafe: Boolean = true,
            testOnly: Boolean = false,
            bounceable: Boolean = true
        ): String {
            checkAddressStd(address)
            return AddrStd.toString(address, userFriendly, urlSafe, testOnly, bounceable)
        }

        @JvmStatic
        public fun parse(address: String): MsgAddressInt = AddrStd.parse(address)

        @JvmStatic
        public fun parseRaw(address: String): MsgAddressInt = AddrStd.parseRaw(address)

        @JvmStatic
        public fun parseUserFriendly(address: String): MsgAddressInt = AddrStd.parseUserFriendly(address)

        private fun checkAddressStd(value: MsgAddressInt) {
            contract {
                returns() implies (value is AddrStd)
            }
            require(value is AddrStd) {
                "expected class: ${AddrStd::class} actual: ${value::class}"
            }
        }
    }
}

private object MsgAddressIntTlbCombinator : TlbCombinator<MsgAddressInt>(
    MsgAddressInt::class,
    AddrStd::class to AddrStd.tlbCodec(),
    AddrVar::class to AddrVar.tlbCodec()
)
