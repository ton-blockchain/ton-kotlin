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
 * Variable-length internal address.
 *
 * ```tlb
 * addr_var$11 anycast:(Maybe Anycast) addr_len:(## 9)
 *    workchain_id:int32 address:(bits addr_len) = MsgAddressInt;
 * ```
 */
public data class AddrVar(
    override val anycast: Anycast?,
    override val workchain: Int,
    override val address: BitString
) : MsgAddressInt {
    public constructor(anycast: Anycast?, workchain: Int, address: ByteArray) : this(
        anycast,
        workchain,
        address.toBitString()
    )

    public constructor(anycast: Anycast?, workchain: Int, address: ByteString) : this(
        anycast,
        workchain,
        address.toBitString()
    )

    public constructor(workchain: Int, address: ByteArray) : this(null, workchain, address)
    public constructor(workchain: Int, address: ByteString) : this(null, workchain, address)
    public constructor(workchain: Int, address: BitString) : this(null, workchain, address)

    override val cellSize: CellSize
        get() = CellSize(2 + 1 + 9 + 32 + address.size, 0).let { cellSize ->
            anycast?.let { anycast -> anycast.cellSize + cellSize } ?: cellSize
        }

    override fun toAddrStd(): AddrStd = AddrStd(anycast, workchain, address)

    public companion object : TlbCodec<AddrVar> {
        override fun loadTlb(slice: CellSlice, context: CellContext): AddrVar =
            MsgAddressInt.loadTlb(slice, context) as AddrVar

        override fun storeTlb(builder: CellBuilder, value: AddrVar, context: CellContext): Unit =
            MsgAddressInt.storeTlb(builder, value, context)
    }
}
