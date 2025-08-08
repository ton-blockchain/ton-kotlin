@file:OptIn(ExperimentalTime::class)

package org.ton.kotlin.dht.bucket

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * [PendingNode] is a [Node] that is pending insertion into a [KBucket].
 */
internal data class PendingNode<K, V>(
    val node: Node<K, V>,
    val status: NodeStatus,
    val replace: Instant
) {
    fun isReady(now: Instant = Clock.System.now()): Boolean = now >= replace
}
