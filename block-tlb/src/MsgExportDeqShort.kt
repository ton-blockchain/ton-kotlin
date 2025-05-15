package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


@SerialName("msg_export_deq_short")
public data class MsgExportDeqShort(
    val msgEnvHash: BitString,
    val nextWorkchain: Int,
    val nextAddrPfx: ULong,
    val importBlockLt: ULong
) : OutMsg {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("msg_export_deq_short") {
            field("msg_env_hash", msgEnvHash)
            field("next_workchain", nextWorkchain)
            field("next_addr_pfx", nextAddrPfx)
            field("import_block_lt", importBlockLt)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<MsgExportDeqShort> by MsgExportDeqShortTlbConstructor
}

private object MsgExportDeqShortTlbConstructor : TlbConstructor<MsgExportDeqShort>(
    schema = "msg_export_deq_short\$1101 msg_env_hash:bits256 " +
            "next_workchain:int32 next_addr_pfx:uint64 " +
            "import_block_lt:uint64 = OutMsg;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: MsgExportDeqShort
    ) = cellBuilder {
        storeBits(value.msgEnvHash)
        storeInt(value.nextWorkchain, 32)
        storeUInt64(value.nextAddrPfx)
        storeUInt64(value.importBlockLt)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): MsgExportDeqShort = cellSlice {
        val msgEnvHash = loadBits(256)
        val nextWorkchain = loadTinyInt(32).toInt()
        val nextAddrPfx = loadUInt64()
        val importBlockLt = loadUInt64()
        MsgExportDeqShort(msgEnvHash, nextWorkchain, nextAddrPfx, importBlockLt)
    }
}
