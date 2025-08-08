@file:OptIn(ExperimentalTime::class)

package org.ton.kotlin.dht.bucket

import org.ton.kotlin.dht.K_VALUE
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

internal class KBucket<K, V>(
    val index: Int,
    val capacity: Int = K_VALUE,
    val nodes: ArrayList<Node<K, V>> = ArrayList(capacity),
    val pendingTimeout: Duration = 60.seconds,
) : Iterable<Pair<Node<K, V>, NodeStatus>> {
    private var firstConnectedPos = -1
    private var pending: PendingNode<K, V>? = null

    constructor(index: Int, config: KBucketConfig) : this(
        index = index,
        capacity = config.bucketSize,
        pendingTimeout = config.pendingTimeout
    )

    override fun iterator(): Iterator<Pair<Node<K, V>, NodeStatus>> = iterator {
        nodes.forEachIndexed { index, node ->
            yield(node to status(index))
        }
    }

    fun isEmpty(): Boolean = nodes.isEmpty()

    fun size(): Int = nodes.size

    fun pending(key: K): PendingNode<K, V>? {
        val pending = pending ?: return null
        return if (pending.node.key == key) {
            pending
        } else {
            null
        }
    }

    fun position(key: K): Int = nodes.indexOfFirst { it.key == key }

    fun node(key: K): Node<K, V>? {
        return nodes.firstOrNull { it.key == key }
    }

    fun get(key: K): Entry<K, V>? {
        val node = node(key)
        if (node != null) {
            return Entry.Present(node, status(position(key)))
        }
        val pending = pending(key)
        if (pending != null) {
            return Entry.Pending(pending)
        }
        return null
    }

    fun status(position: Int): NodeStatus {
        return if (firstConnectedPos in 0..position) {
            NodeStatus.Connected
        } else {
            NodeStatus.Disconnected
        }
    }

    /**
     * Updates the status of the node referred to by the given key, if it is in the bucket.
     */
    fun update(key: K, status: NodeStatus) {
        val (node, _, pos) = remove(key) ?: return
        if (pos == 0 && status == NodeStatus.Connected) {
            pending = null
        }
        when (add(node, status)) {
            InsertResult.Inserted -> {}
            else -> error("The node is removed before being (re)inserted.")
        }
    }

    /**
     * Adds a new node into the bucket with the given status.
     *
     * The status of the node to insert determines the result as follows:
     *
     * * [NodeStatus.Connected]: If the bucket is full and either all nodes are connected or
     *   there is already a pending node, insertion fails with [InsertResult.Full].
     *   If the bucket is full but at least one node is disconnected and there is no pending node,
     *   the new node is inserted as pending, yielding [InsertResult.Pending].
     *   Otherwise, the bucket has free slots, and the new node is added to the end of the bucket as the most-recently
     *   connected node.
     *
     * * [NodeStatus.Disconnected]: If the bucket is full, insertion fails with
     *   [InsertResult.Full]. Otherwise, the bucket has free slots and the new node is inserted
     *   at the position preceding the first connected node, i.e. as the most-recently
     *   disconnected node. If there are no connected nodes, the new node is added as the last
     *   element of the bucket.
     */
    fun add(node: Node<K, V>, status: NodeStatus) = add(node, status, Clock.System.now())
    fun add(node: Node<K, V>, status: NodeStatus, now: Instant = Clock.System.now()): InsertResult<K> {
        return when (status) {
            NodeStatus.Connected -> {
                if (nodes.size >= capacity) {
                    return if (firstConnectedPos == 0 || pending != null) {
                        InsertResult.Full
                    } else {
                        pending = PendingNode(
                            node = node,
                            status = NodeStatus.Connected,
                            replace = now + pendingTimeout
                        )
                        InsertResult.Pending(disconnected = node.key)
                    }
                }
                val pos = nodes.size
                if (firstConnectedPos < 0) {
                    firstConnectedPos = pos
                }
                nodes.add(node)
                InsertResult.Inserted
            }

            NodeStatus.Disconnected -> {
                if (nodes.size >= capacity) {
                    return InsertResult.Full
                }
                if (firstConnectedPos < 0) {
                    nodes.add(node)
                } else {
                    nodes.add(firstConnectedPos, node)
                    firstConnectedPos++
                }
                InsertResult.Inserted
            }
        }
    }

    /**
     * Removes the node with the given key from the bucket, if it exists.
     */
    fun remove(key: K): Triple<Node<K, V>, NodeStatus, Int>? {
        val position = position(key)
        if (position < 0) return null

        val status = status(position)
        val node = nodes.removeAt(position)
        when (status) {
            NodeStatus.Connected -> {
                if (firstConnectedPos == position && position == nodes.size) {
                    firstConnectedPos = -1
                }
            }

            NodeStatus.Disconnected -> {
                if (firstConnectedPos >= 0) {
                    firstConnectedPos--
                }
            }
        }
        return Triple(node, status, position)
    }

    sealed interface Entry<K, V> {
        /** The entry is a node in the bucket. */
        data class Present<K, V>(val node: Node<K, V>, val status: NodeStatus) : Entry<K, V>

        /** The entry is a pending node in the bucket. */
        data class Pending<K, V>(val pending: PendingNode<K, V>) : Entry<K, V>
    }

    /**
     * The result of inserting an entry into a bucket.
     */
    sealed interface InsertResult<out K> {
        /** The entry has been successfully inserted. */
        object Inserted : InsertResult<Nothing>

        /**
         * The entry is pending insertion because the relevant bucket is currently full.
         * The entry is inserted after a timeout elapsed, if the status of the
         * least-recently connected (and currently disconnected) node in the bucket
         * is not updated before the timeout expires.
         */
        data class Pending<K>(val disconnected: K) : InsertResult<K>

        /** The entry was not inserted because the relevant bucket is full. */
        object Full : InsertResult<Nothing>
    }
}
