package org.ton.block.message.output

import org.ton.bitstring.BitString
import org.ton.cell.buildCell
import org.ton.cell.parse
import org.ton.kotlin.dict.DictionaryKeyCodec

public data class ProcessedInfoKey(
    val shard: Long,
    val mcSeqno: Int
) {
    public companion object : DictionaryKeyCodec<ProcessedInfoKey> {
        override val keySize: Int = 64 + 32

        override fun decodeKey(value: BitString): ProcessedInfoKey {
            return buildCell {
                storeBitString(value)
            }.parse {
                val shard = loadULong(64).toLong()
                val mcSeqno = loadUInt(32).toInt()
                ProcessedInfoKey(shard, mcSeqno)
            }
        }

        override fun encodeKey(value: ProcessedInfoKey): BitString {
            return buildCell {
                storeULong(value.shard.toULong(), 64)
                storeUInt(value.mcSeqno, 32)
            }.bits
        }
    }
}