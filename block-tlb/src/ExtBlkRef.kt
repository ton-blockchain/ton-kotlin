package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbObject
import org.ton.kotlin.tlb.TlbPrettyPrinter

@SerialName("ext_blk_ref")

public data class ExtBlkRef(
    @SerialName("end_lt") val endLt: ULong, // end_lt : uint64
    @SerialName("seq_no") val seqNo: UInt, // seq_no : uint32
    @SerialName("root_hash") val rootHash: BitString, // root_hash : bits256
    @SerialName("file_hash") val fileHash: BitString // file_hash : bits256
) : TlbObject {
    init {
        require(rootHash.size == 256)
        require(fileHash.size == 256)
    }

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("ext_blk_ref") {
        field("end_lt", endLt)
        field("seq_no", seqNo)
        field("root_hash", rootHash)
        field("file_hash", fileHash)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCodec<ExtBlkRef> by ExtBlkRefTlbConstructor
}

private object ExtBlkRefTlbConstructor : TlbConstructor<ExtBlkRef>(
    schema = "ext_blk_ref\$_ end_lt:uint64 " +
            "seq_no:uint32 root_hash:bits256 file_hash:bits256 " +
            "= ExtBlkRef;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: ExtBlkRef
    ) = cellBuilder {
        storeUInt64(value.endLt)
        storeUInt32(value.seqNo)
        storeBits(value.rootHash)
        storeBits(value.fileHash)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): ExtBlkRef = cellSlice {
        val endLt = loadUInt64()
        val seqNo = loadUInt32()
        val rootHash = loadBits(256)
        val fileHash = loadBits(256)
        ExtBlkRef(endLt, seqNo, rootHash, fileHash)
    }
}
