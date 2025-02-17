@file:Suppress("NOTHING_TO_INLINE", "PropertyName", "PackageDirectoryMismatch")

package org.ton.kotlin.message.address

import org.ton.bitstring.BitString
import org.ton.bitstring.ByteBackedMutableBitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellContext
import org.ton.tlb.TlbCodec
import kotlin.contracts.contract
import kotlin.jvm.JvmStatic

public inline fun MsgAddressInt(address: String): MsgAddressInt = MsgAddressInt.parse(address)

public sealed interface MsgAddressInt : MsgAddress {
    public val anycast: Anycast?
    public val workchain: Int
    public val address: BitString

    public val prefix: BitString
        get() {
            val result = ByteBackedMutableBitString(64)
            val address = address
            val anycast = anycast?.rewritePrefix
            if (anycast != null) {
                val depth = anycast.size
                anycast.copyInto(result)
                val addressSize = address.size
                if (addressSize > depth) {
                    address.copyInto(result, depth, depth, addressSize - depth)
                }
            } else {
                address.copyInto(result)
            }
            return result
        }

    @Deprecated("Use workchain instead", replaceWith = ReplaceWith("workchain"))
    public val workchainId: Int get() = workchain

    public fun toAddrStd(): AddrStd

    public companion object : TlbCodec<MsgAddressInt> {
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

        override fun loadTlb(slice: CellSlice, context: CellContext): MsgAddressInt {
            val tag = slice.loadUInt(3).toInt()
            return when (tag) {
                0b100 -> { // addr_std$10, anycast=nothing$0
                    val workchain = slice.loadInt(8).toInt()
                    val address = slice.loadBitString(256)
                    AddrStd(null, workchain, address)
                }

                0b101 -> { // addr_std$10, anycast=just$1
                    val anycast = Anycast.loadTlb(slice, context)
                    val workchain = slice.loadInt(8).toInt()
                    val address = slice.loadBitString(256)
                    AddrStd(anycast, workchain, address)
                }

                0b110 -> { // addr_var$11, anycast=nothing$0
                    val len = slice.loadUInt(9).toInt()
                    val workchain = slice.loadInt(32).toInt()
                    val address = slice.loadBitString(len)
                    AddrVar(null, workchain, address)
                }

                0b111 -> { // addr_var$11, anycast=just$1
                    val anycast = Anycast.loadTlb(slice, context)
                    val len = slice.loadUInt(9).toInt()
                    val workchain = slice.loadInt(32).toInt()
                    val address = slice.loadBitString(len)
                    AddrVar(anycast, workchain, address)
                }

                else -> throw IllegalArgumentException("Invalid address tag: $tag")
            }
        }

        override fun storeTlb(builder: CellBuilder, value: MsgAddressInt, context: CellContext) {
            when (value) {
                is AddrStd -> {
                    if (value.anycast == null) {
                        // addr_std$10, anycast=nothing$0
                        builder.storeUInt(0b100, 3)
                    } else {
                        // addr_std$10, anycast=just$1
                        builder.storeUInt(0b101, 3)
                    }
                    builder.storeInt(value.workchain, 8)
                    builder.storeBitString(value.address)
                }

                is AddrVar -> {
                    if (value.anycast == null) {
                        // addr_var$11, anycast=nothing$0
                        builder.storeUInt(0b110, 3)
                    } else {
                        // addr_var$11, anycast=just$1
                        builder.storeUInt(0b111, 3)
                    }
                    builder.storeUInt(value.address.size, 9)
                    builder.storeInt(value.workchain, 32)
                    builder.storeBitString(value.address)
                }
            }
        }
    }
}
