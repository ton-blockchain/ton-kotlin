package org.ton.kotlin.dht.bucket

/**
 * The result of applying a pending node to a bucket, possibly
 *  replacing an existing node.
 */
data class AppliedPending<K, V>(
    val inserted: Node<K, V>,
    val evicted: Node<K, V>?,
)
