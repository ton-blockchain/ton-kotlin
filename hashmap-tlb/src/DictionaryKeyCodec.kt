@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.dict

import kotlinx.io.bytestring.ByteString
import org.ton.bitstring.BitString
import org.ton.cell.CellSlice
import org.ton.cell.buildCell

public interface DictionaryKeyCodec<K> : DictionaryKeyLoader<K>, DictionaryKeyStorer<K> {
    public companion object {
        public val INT32: DictionaryKeyCodec<Int> = int()
        public val LONG64: DictionaryKeyCodec<Long> = long()

        public val BITS256: DictionaryKeyCodec<BitString> = object : DictionaryKeyCodec<BitString> {
            override val keySize: Int get() = 256

            override fun decodeKey(value: BitString): BitString {
                require(value.size == 256)
                return value
            }

            override fun encodeKey(value: BitString): BitString {
                require(value.size == 256)
                return value
            }
        }

        public val BYTE_STRING_32: DictionaryKeyCodec<ByteString> = object : DictionaryKeyCodec<ByteString> {
            override val keySize: Int get() = 256

            override fun decodeKey(value: BitString): ByteString {
                return ByteString(*value.toByteArray())
            }

            override fun encodeKey(value: ByteString): BitString {
                return BitString(value.toByteArray())
            }
        }

        public fun long(keySize: Int = Long.SIZE_BITS): DictionaryKeyCodec<Long> = object : DictionaryKeyCodec<Long> {
            override val keySize: Int = keySize

            override fun decodeKey(value: BitString): Long {
                return CellSlice(value).loadLong(keySize)
            }

            override fun encodeKey(value: Long): BitString {
                return buildCell {
                    storeLong(value, keySize)
                }.bits
            }
        }

        public fun int(keySize: Int = Int.SIZE_BITS): DictionaryKeyCodec<Int> = object : DictionaryKeyCodec<Int> {
            override val keySize: Int = keySize

            override fun decodeKey(value: BitString): Int {
                return CellSlice(value).loadInt(keySize).toInt()
            }

            override fun encodeKey(value: Int): BitString {
                return buildCell {
                    storeInt(value, keySize)
                }.bits
            }
        }
    }
}

public interface DictionaryKeyLoader<out K> {
    public val keySize: Int

    public fun decodeKey(value: BitString): K
}

public interface DictionaryKeyStorer<in K> {
    public val keySize: Int

    public fun encodeKey(value: K): BitString
}
