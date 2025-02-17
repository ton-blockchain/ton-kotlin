@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.message.address

import kotlinx.io.bytestring.ByteString
import org.ton.bitstring.BitString
import org.ton.bitstring.toBitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSize
import org.ton.tlb.TlbCodec

/**
 * External address.
 *
 * ```tlb
 * addr_extern$01 len:(## 9) external_address:(bits len) = MsgAddressExt;
 * ```
 */
public data class AddrExtern(
    val address: BitString
) : MsgAddress {
    public constructor(address: ByteArray) : this(address.toBitString())
    public constructor(address: ByteString) : this(address.toBitString())

    init {
        require(address.size <= 512) { "Address length must be in 0..512, actual: ${address.size}" }
    }

    override val cellSize: CellSize get() = CellSize(9 + address.size, 0)

    @Deprecated("Use address instead", ReplaceWith("address"))
    public val externalAddress: BitString get() = address

    public companion object : TlbCodec<AddrExtern?> {
        override fun loadTlb(slice: CellSlice, context: CellContext): AddrExtern? {
            val tag = slice.loadUInt(2).toInt()
            return when (tag) {
                0b00 -> null
                0b01 -> { // addr_extern$01
                    val len = slice.loadUInt(9).toInt()
                    val address = slice.loadBitString(len)
                    AddrExtern(address)
                }

                else -> throw IllegalArgumentException("unknown extern_addr tag: $tag")
            }
        }

        override fun storeTlb(builder: CellBuilder, value: AddrExtern?, context: CellContext) {
            if (value == null) {
                // addr_none$00
                builder.storeUInt(0b00, 2)
            } else {
                // addr_extern$01
                builder.storeUInt(0b01, 2)
                builder.storeUInt(value.address.size, 9)
                builder.storeBitString(value.address)
            }
        }
    }
}