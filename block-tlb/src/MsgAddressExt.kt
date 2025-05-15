@file:Suppress("OPT_IN_USAGE", "NOTHING_TO_INLINE")

package org.ton.kotlin.block

import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.bitstring.isNullOrEmpty
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.TlbCombinator
import kotlin.jvm.JvmStatic

public inline fun MsgAddressExt(externalAddress: BitString? = null): MsgAddressExt = MsgAddressExt.of(externalAddress)

public inline fun MsgAddressExt(externalAddress: ByteArray): MsgAddressExt = MsgAddressExt.of(externalAddress)

@JsonClassDiscriminator("@type")

public sealed interface MsgAddressExt : MsgAddress {
    public companion object : TlbCodec<MsgAddressExt> by MsgAddressExtTlbCombinator {
        @JvmStatic
        public fun of(externalAddress: BitString? = null): MsgAddressExt {
            return if (externalAddress.isNullOrEmpty()) {
                AddrNone
            } else {
                AddrExtern(externalAddress)
            }
        }

        @JvmStatic
        public fun of(externalAddress: ByteArray): MsgAddressExt = AddrExtern(externalAddress)

        @JvmStatic
        public fun tlbCodec(): TlbCombinator<MsgAddressExt> = MsgAddressExtTlbCombinator
    }
}

private object MsgAddressExtTlbCombinator : TlbCombinator<MsgAddressExt>(
    MsgAddressExt::class,
    AddrNone::class to AddrNone.tlbConstructor(),
    AddrExtern::class to AddrExtern.tlbConstructor(),
)
