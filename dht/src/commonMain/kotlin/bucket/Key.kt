package org.ton.kotlin.dht.bucket

import kotlinx.io.bytestring.ByteString

interface Key {
    val hash: ByteString

    fun distance(target: Key): Distance = Distance(hash, target.hash)

    companion object {
        private val AFFINITY_BITS = byteArrayOf(4, 3, 2, 2, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0)

        fun affinity(
            key1: ByteString,
            key2: ByteString
        ): Int {
            var result = 0
            for (i in 0 until 32) {
                val x = key1[i].toInt() xor key2[i].toInt()
                print(x.toUByte().toString(2).padStart(8, '0') + " ")
                result += if (x == 0) {
                    8
                } else {
                    if (x and 0xF0 == 0) {
                        AFFINITY_BITS[(x.toUByte() and 0x0Fu).toInt()].toInt() + 4
                    } else {
                        AFFINITY_BITS[(x.toUByte().toInt() shr 4)].toInt()
                    }
                }
            }
            return result
        }
    }
}
