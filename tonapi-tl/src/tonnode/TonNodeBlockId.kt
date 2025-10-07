@file:Suppress("NOTHING_TO_INLINE")

package org.ton.api.tonnode

import kotlinx.serialization.Serializable
import org.ton.tl.TlCodec
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter
import kotlin.jvm.JvmStatic

public inline fun TonNodeBlockId(
    workchain: Int = Workchain.INVALID_WORKCHAIN,
    shard: Long = 0,
    seqno: Int = 0
): TonNodeBlockId = TonNodeBlockId.of(workchain, shard, seqno)

public inline fun TonNodeBlockId(string: String): TonNodeBlockId = TonNodeBlockId.parse(string)

public interface TonNodeBlockId {
    public val workchain: Int
    public val shard: Long
    public val seqno: Int

    public fun isMasterchain(): Boolean = workchain == Workchain.MASTERCHAIN_ID

    public fun isValid(): Boolean = workchain != Workchain.INVALID_WORKCHAIN
    public fun isValidExt(): Boolean = workchain.toLong() and shard != 0L
    public fun isValidFull(): Boolean =
        isValid() && shard != 0L && shard and 7 <= 0 && (!isMasterchain() || shard == Shard.ID_ALL)

    public fun withSeqno(seqno: Int): TonNodeBlockId = TonNodeBlockIdImpl(workchain, shard, seqno)

    public operator fun component1(): Int = workchain
    public operator fun component2(): Long = shard
    public operator fun component3(): Int = seqno

    public companion object : TlCodec<TonNodeBlockId> by TonNodeBlockIdTlbConstructor {
        @JvmStatic
        public fun of(workchain: Int, shard: Long, seqno: Int): TonNodeBlockId =
            TonNodeBlockIdImpl(workchain, shard, seqno)

        @JvmStatic
        public fun parse(string: String): TonNodeBlockId {
            require(string.getOrNull(0) == '(') { "Can't parse string: '$string'" }
            require(string.getOrNull(string.lastIndex) == ')') { "Can't parse string: '$string'" }
            val (rawWorkchain, rawShard, rawSeqno) = string.substring(1, string.lastIndex).split(':')
            return TonNodeBlockIdImpl(rawWorkchain.toInt(), rawShard.toULong(16).toLong(), rawSeqno.toInt())
        }

        @JvmStatic
        public fun parseOrNull(string: String): TonNodeBlockId? = try {
            parse(string)
        } catch (e: Exception) {
            null
        }
    }
}

@Serializable
private data class TonNodeBlockIdImpl(
    override val workchain: Int,
    override val shard: Long,
    override val seqno: Int
) : TonNodeBlockId {
    override fun withSeqno(seqno: Int): TonNodeBlockId = copy(seqno = seqno)

    override fun toString(): String = "($workchain:${shard.toHexString(HexFormat.UpperCase)}:$seqno)"
}

private object TonNodeBlockIdTlbConstructor : TlConstructor<TonNodeBlockId>(
    schema = "tonNode.blockId workchain:int shard:long seqno:int = tonNode.BlockId"
) {
    override fun decode(reader: TlReader): TonNodeBlockId {
        val workchain = reader.readInt()
        val shard = reader.readLong()
        val seqno = reader.readInt()
        return TonNodeBlockIdImpl(workchain, shard, seqno)
    }

    override fun encode(writer: TlWriter, value: TonNodeBlockId) {
        writer.writeInt(value.workchain)
        writer.writeLong(value.shard)
        writer.writeInt(value.seqno)
    }
}
