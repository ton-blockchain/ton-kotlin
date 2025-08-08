package org.ton.kotlin.dht.bucket

/**
 * The status of a node in a bucket.
 *
 * The status of a node in a bucket together with the time of the
 * last status change determines the position of the node in a
 * bucket.
 */
internal enum class NodeStatus {
    Connected,
    Disconnected;
}
