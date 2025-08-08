package org.ton.kotlin.dht

import Xoroshiro128PlusPlus
import io.github.andreypfau.kotlinx.crypto.sha256
import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.dht.bucket.KademliaRoutingTable
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.time.ExperimentalTime

class KBucketTest {
    fun randomKey(random: Random = Random) = DhtKeyId(
        ByteString(
            sha256(
                byteArrayOf(0x0, 0x20) + random.nextBytes(32)
            )
        )
    )

    @OptIn(ExperimentalTime::class)
    @Test
    fun closest() {
        val random = Xoroshiro128PlusPlus(42)
        val localKey = randomKey(random)
        val table = KademliaRoutingTable<DhtKeyId>(localKey) { it }
        var count = 0
        while (count < 100) {
            val key = randomKey(random)
            if (table.add(key) == null) {
                count++
            }
        }

        val expectedKeys = table.buckets.flatMap { it }
        repeat(10) {
            val targetKey = randomKey(random)

            val keys = table.nearestKeys(targetKey).toList()
            val sortedKeys = expectedKeys.sortedBy { it.distance(targetKey) }

            assertContentEquals(sortedKeys, keys)
        }
    }
}
