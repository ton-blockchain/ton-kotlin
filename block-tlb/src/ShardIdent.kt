package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.sdk.bigint.toInt
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor

@SerialName("shard_ident")

public data class ShardIdent(
    @SerialName("shard_pfx_bits") val shardPfxBits: Int, // shard_pfx_bits : #<= 60
    @SerialName("workchain_id") val workchainId: Int, // workchain_id : int32
    @SerialName("shard_prefix") val shardPrefix: ULong // shard_prefix : uint64
) : TlbObject {
    init {
        require(shardPfxBits <= 60) { "expected: shard_pfx_bits <= 60, actual: $shardPfxBits" }
    }

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("shard_ident") {
        field("shard_pfx_bits", shardPfxBits)
        field("workchain_id", workchainId)
        field("shard_prefix", shardPrefix)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCodec<ShardIdent> by ShardIdentTlbConstructor.asTlbCombinator()
}

private object ShardIdentTlbConstructor : TlbConstructor<ShardIdent>(
    schema = "shard_ident\$00 shard_pfx_bits:(#<= 60) " +
            "workchain_id:int32 shard_prefix:uint64 = ShardIdent;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: ShardIdent
    ) = cellBuilder {
        storeUIntLeq(value.shardPfxBits, 60)
        storeInt(value.workchainId, 32)
        storeUInt64(value.shardPrefix)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): ShardIdent = cellSlice {
        val shardPfxBits = loadUIntLeq(60).toInt()
        val workchainId = loadInt(32).toInt()
        val shardPrefix = loadUInt64()
        ShardIdent(shardPfxBits, workchainId, shardPrefix)
    }
}
