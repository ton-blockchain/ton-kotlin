@file:Suppress("NOTHING_TO_INLINE")

package org.ton.api.tonnode

import org.ton.bitstring.BitString
import org.ton.cell.Cell
import org.ton.cell.CellSlice
import kotlin.jvm.JvmInline

@JvmInline
public value class Shard(
    public val value: Long
) {
    public inline fun child(right: Boolean): Shard = Shard(childShard(value, right))
    public inline fun parent(): Shard = Shard(parentShard(value))

    public inline operator fun contains(child: Shard): Boolean = containsShard(value, child.value)

    public inline fun hex(): String = value.toULong().toString(16).padStart(16, '0')
    public inline fun bin(): String = value.toULong().toString(2).padStart(64, '0')

    public inline fun toLong(): Long = value

    public companion object {
        public const val ID_ALL: Long = 1L shl 63
        public val ALL: Shard = ID_ALL.toShard()

        public fun extractShard(bits: BitString): Long {
            return CellSlice(bits).loadULong().toLong()
        }

        public fun containsShard(parent: Long, child: Long): Boolean {
            val x = lowerBits64(parent)
            return ((parent xor child) and (bitsNegative64(x) shl 1)) == 0L
        }

        public fun childShard(shard: Long, right: Boolean): Long {
            val x = lowerBits64(shard) ushr 1
            return if (right) shard + x else shard - x
        }

        public fun parentShard(shard: Long): Long {
            val x = lowerBits64(shard)
            return (shard - x) or (x shl 1)
        }

        public fun check(block: TonNodeBlockIdExt, shardBlock: TonNodeBlockIdExt, shardProof: Cell) {
            require(block.isMasterchain() && block.isValidFull()) { "block must belong to the masterchain" }
        }

        internal inline fun lowerBits64(x: Long) = x and bitsNegative64(x)
        internal inline fun bitsNegative64(x: Long) = x.inv() + 1
    }
}

public inline fun Long.toShard(): Shard = Shard(this)
public inline fun ULong.toShard(): Shard = Shard(this.toLong())
public inline fun Shard.toULong(): ULong = toLong().toULong()
