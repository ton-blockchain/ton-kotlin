package org.ton.block

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.hashmap.HashmapAugE
import org.ton.kotlin.account.ShardAccount
import org.ton.kotlin.cell.CellContext
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbObject
import org.ton.tlb.TlbPrettyPrinter
import kotlin.jvm.JvmInline


@JvmInline
public value class ShardAccounts(
    public val x: HashmapAugE<ShardAccount, DepthBalanceInfo>
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return x.print(printer)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCodec<ShardAccounts> by ShardAccountsTlbCodec
}

private object ShardAccountsTlbCodec : TlbCodec<ShardAccounts> {
    private val codec = HashmapAugE.tlbCodec(256, ShardAccount, DepthBalanceInfo)

    override fun storeTlb(cellBuilder: CellBuilder, value: ShardAccounts, context: CellContext) {
        codec.storeTlb(cellBuilder, value.x, context)
    }

    override fun loadTlb(cellSlice: CellSlice, context: CellContext): ShardAccounts {
        return ShardAccounts(codec.loadTlb(cellSlice, context))
    }
}
