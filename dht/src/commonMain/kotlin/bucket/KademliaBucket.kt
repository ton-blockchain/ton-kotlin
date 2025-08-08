package org.ton.kotlin.dht.bucket

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

class KademliaBucket<E>(
    val index: Int,
    private val entries: MutableList<E> = mutableListOf(),
    private val k: Int,
    private val maxReplacements: Int
) : List<E> by entries {
    constructor(index: Int, k: Int, maxReplacements: Int) : this(index, ArrayList(k), k, maxReplacements)

    private val replacementCache = ArrayList<E>(maxReplacements)
    private val lock = reentrantLock()

    fun add(node: E): E? = lock.withLock {
        // remove from the replacement cache, if present
        replacementCache.remove(node)

        // check if the list contains this node, and move to the front if so
        for (i in entries.indices) {
            if (entries[i] == node) {
                // already in table, so move to front
                entries.removeAt(i)
                entries.add(0, node)
                return null
            }
        }

        if (entries.size == k) {
            // bucket is full, so add to the replacement cache
            if (replacementCache.size == maxReplacements) {
                replacementCache.removeAt(0)
            }
            replacementCache.add(node)
            return entries.last()
        }

        // add entry to the front of the bucket
        entries.add(0, node)
        return null
    }

    fun evict(node: E): Boolean = lock.withLock {
        if (!entries.remove(node)) {
            return false
        }
        if (!replacementCache.isEmpty()) {
            val replacement = replacementCache.removeAt(replacementCache.lastIndex)
            entries.add(0, replacement)
        }
        return true
    }

    fun clear() = lock.withLock {
        entries.clear()
        replacementCache.clear()
    }
}
