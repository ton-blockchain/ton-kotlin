package org.ton.kotlin.dht

import org.ton.kotlin.dht.bucket.Key

interface RoutingTable<T> {

    fun add(node: T): T?

    fun evict(node: T): Boolean

    /**
     * Returns a [Sequence] over the keys closest to [target],
     * ordered by increasing distance.
     */
    fun nearestKeys(target: Key): Sequence<Key>

    /**
     * Returns a [Sequence] over the entries closest to the [target] key,
     * ordered by increasing distance.
     */
    fun nearest(target: Key): Sequence<T>
}
