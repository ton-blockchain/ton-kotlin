package org.ton.block.message.output

import kotlinx.io.bytestring.ByteString
import org.ton.bitstring.BitString
import org.ton.cell.buildCell
import org.ton.cell.parse
import org.ton.kotlin.dict.DictionaryKeyCodec

public data class OutMsgQueueKey(
    val workchain: Int,
    val prefix: BitString,
    val hash: ByteString
) {
    public companion object : DictionaryKeyCodec<OutMsgQueueKey> {
        override val keySize: Int = 32 + 64 + 256

        override fun decodeKey(value: BitString): OutMsgQueueKey {
            return buildCell {
                storeBitString(value)
            }.parse {
                val workchain = loadInt(32).toInt()
                val prefix = loadBitString(64)
                val hash = loadByteString(32)
                OutMsgQueueKey(workchain, prefix, hash)
            }
        }

        override fun encodeKey(value: OutMsgQueueKey): BitString {
            return buildCell {
                storeInt(value.workchain, 32)
                storeBitString(value.prefix)
                storeByteString(value.hash)
            }.bits
        }
    }
}