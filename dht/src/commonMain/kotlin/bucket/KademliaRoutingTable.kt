package org.ton.kotlin.dht.bucket

import org.ton.kotlin.dht.RoutingTable


private const val BUCKET_NUMBER = 256

class KademliaRoutingTable<T> private constructor(
    val localKey: Key,
    val buckets: Array<KademliaBucket<T>>,
    val keyForNode: (T) -> Key
) : RoutingTable<T>, Iterable<KademliaBucket<T>> {
    constructor(
        localKey: Key,
        config: KBucketConfig = KBucketConfig(),
        keyForNode: (T) -> Key,
    ) : this(
        localKey,
        buckets = Array(BUCKET_NUMBER) { index -> KademliaBucket(index, config.bucketSize, config.bucketSize) },
        keyForNode,
    )

    fun bucket(node: T): KademliaBucket<T>? {
        val index = localKey.distance(keyForNode(node)).ilog2() ?: return null
        val bucket = buckets[index]
        return bucket
    }

    override fun add(node: T): T? = bucket(node)?.add(node)

    override fun evict(node: T): Boolean = bucket(node)?.evict(node) ?: false

    override fun nearestKeys(target: Key): Sequence<Key> {
        val distance = localKey.distance(target)
        return closestBuckets(distance).flatMap { bucket ->
            bucket.asSequence().map {
                keyForNode(it)
            }.sortedWith { a, b ->
                target.distance(a).compareTo(target.distance(b))
            }
        }
    }

    override fun nearest(target: Key): Sequence<T> {
        val distance = localKey.distance(target)
        return closestBuckets(distance).flatMap { bucket ->
            bucket.asSequence().sortedWith { a, b ->
                target.distance(keyForNode(a)).compareTo(target.distance(keyForNode(b)))
            }
        }
    }

    private fun closestBuckets(
        distance: Distance
    ) = sequence {
        val initial = distance.ilog2() ?: 0
        yield(buckets[initial])
        for (i in initial - 1 downTo 0) {
            val bit = distance.bit(255 - i)
            if (bit) yield(buckets[i])
        }
        yield(buckets[0])
        for (i in 1 until BUCKET_NUMBER) {
            val bit = distance.bit(255 - i)
            if (!bit) yield(buckets[i])
        }
    }

    override fun iterator(): Iterator<KademliaBucket<T>> = iterator {
        for (bucket in buckets) {
            if (bucket.isEmpty()) continue
            yield(bucket)
        }
    }
}
