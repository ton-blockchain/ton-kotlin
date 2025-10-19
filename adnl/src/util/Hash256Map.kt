package org.ton.kotlin.adnl.util

import kotlinx.io.bytestring.ByteString
import kotlin.concurrent.atomics.AtomicArray
import kotlin.concurrent.atomics.ExperimentalAtomicApi

public class Hash256Map<K, V>(
    private val hashFunction: (K) -> ByteString
) : AbstractMutableMap<K, V>() {
    private val buckets = Array<Bucket>(256) { Bucket() }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> =
        object : MutableSet<MutableMap.MutableEntry<K, V>> {
            override val size: Int
                get() = this@Hash256Map.size

            override fun add(element: MutableMap.MutableEntry<K, V>): Boolean {
                val exists = contains(element)
                put(element.key, element.value)
                return !exists
            }

            override fun addAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
                var changed = false
                for (e in elements) {
                    changed = changed or add(e)
                }
                return changed
            }

            override fun clear() {
                this@Hash256Map.clear()
            }

            @OptIn(ExperimentalAtomicApi::class)
            override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> {
                val iter = iterator {
                    for (bucket in buckets) {
                        val segments = bucket.segments
                        for (i in 0 until segments.size) {
                            val list = segments.loadAt(i)
                            for (entry in list) {
                                yield(entry)
                            }
                        }
                    }
                }

                return object : MutableIterator<MutableMap.MutableEntry<K, V>> {
                    private var current: MutableMap.MutableEntry<K, V>? = null

                    override fun hasNext(): Boolean = iter.hasNext()

                    override fun next(): MutableMap.MutableEntry<K, V> {
                        val next = iter.next()
                        current = next
                        return next
                    }

                    override fun remove() {
                        current?.let { this@Hash256Map.remove(it.key) }
                    }
                }
            }

            override fun contains(element: MutableMap.MutableEntry<K, V>): Boolean {
                val v = get(element.key)
                return v == element.value
            }

            override fun containsAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
                return elements.all { contains(it) }
            }

            override fun isEmpty(): Boolean = this@Hash256Map.isEmpty()

            override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean {
                val v = get(element.key)
                if (v == element.value) {
                    this@Hash256Map.remove(element.key)
                    return true
                }
                return false
            }

            override fun removeAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
                var changed = false
                for (e in elements) {
                    changed = changed or remove(e)
                }
                return changed
            }

            override fun retainAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
                val toKeep = elements.map { it.key }.toSet()
                val toRemove = this@Hash256Map.keys.filter { it !in toKeep }
                for (k in toRemove) {
                    this@Hash256Map.remove(k)
                }
                return toRemove.isNotEmpty()
            }
        }


    override fun put(key: K, value: V): V? {
        val hash = hashFunction(key)
        return buckets[hash[0].toInt() and 0xFF].putHashedValue(hash, key, value)
    }

    override fun get(key: K): V? {
        val hash = hashFunction(key)
        return buckets[hash[0].toInt() and 0xFF].get(hash)
    }

    override fun containsKey(key: K): Boolean {
        val hash = hashFunction(key)
        return buckets[hash[0].toInt() and 0xFF].containsHash(hash)
    }

    override fun remove(key: K): V? {
        val hash = hashFunction(key)
        return buckets[hash[0].toInt() and 0xFF].remove(hash)
    }

    public fun putIfAbsent(key: K, value: V): V? {
        val hash = hashFunction(key)
        return buckets[hash[0].toInt() and 0xFF].putIfAbsentHashedValue(hash, key, value)
    }

    override fun clear() {
        for (bucket in buckets) {
            bucket.clear()
        }
    }

    override val size: Int
        get() = buckets.sumOf { it.size }

    private inner class Bucket() {
        @OptIn(ExperimentalAtomicApi::class)
        val segments = AtomicArray<List<Entry>>(16) {
            emptyList<Entry>()
        }

        @OptIn(ExperimentalAtomicApi::class)
        val size: Int
            get() {
                var sum = 0
                for (i in 0 until 16) {
                    sum += segments.loadAt(i).size
                }
                return sum
            }

        @OptIn(ExperimentalAtomicApi::class)
        fun putHashedValue(hash: ByteString, key: K, value: V): V? {
            val segmentIdx = hash[1].toInt() and 0x0F
            while (true) {
                val oldList = segments.loadAt(segmentIdx)
                val idx = oldList.findKeyIndex(hash)
                val newList: List<Entry>
                val result: V?
                if (idx >= 0) {
                    result = oldList[idx].value
                    newList = ArrayList(oldList)
                    newList[idx] = Entry(this, hash, key, value)
                } else {
                    result = null
                    newList = ArrayList(oldList.size + 1)
                    newList.addAll(oldList)
                    newList.add(-idx - 1, Entry(this, hash, key, value))
                }
                if (segments.compareAndSetAt(segmentIdx, oldList, newList)) {
                    return result
                }
            }
        }

        @OptIn(ExperimentalAtomicApi::class)
        fun remove(key: ByteString): V? {
            val segmentIdx = key[1].toInt() and 0x0F
            while (true) {
                val oldList = segments.loadAt(segmentIdx)
                val idx = oldList.findKeyIndex(key)
                if (idx < 0) {
                    return null
                }
                val newList = ArrayList(oldList)
                val removed = newList.removeAt(idx).value
                if (segments.compareAndSetAt(segmentIdx, oldList, newList)) {
                    return removed
                }
            }
        }

        @OptIn(ExperimentalAtomicApi::class)
        fun clear() {
            for (i in 0 until 16) {
                segments.storeAt(i, emptyList())
            }
        }

        @OptIn(ExperimentalAtomicApi::class)
        fun get(hash: ByteString): V? {
            val segmentIndex = hash[1].toInt() and 0x0F
            val list = segments.loadAt(segmentIndex)
            val idx = list.findKeyIndex(hash)
            return if (idx >= 0) {
                list[idx].value
            } else {
                null
            }
        }

        @OptIn(ExperimentalAtomicApi::class)
        fun putIfAbsentHashedValue(hash: ByteString, key: K, value: V): V? {
            val segmentIdx = hash[1].toInt() and 0x0F
            while (true) {
                val oldList = segments.loadAt(segmentIdx)
                val idx = oldList.findKeyIndex(hash)
                if (idx >= 0) {
                    return oldList[idx].value
                }
                val newList = ArrayList<Entry>(oldList.size + 1).apply {
                    addAll(oldList)
                    add(-idx - 1, Entry(this@Bucket, hash, key, value))
                }
                if (segments.compareAndSetAt(segmentIdx, oldList, newList)) {
                    return null
                }
            }
        }

        @OptIn(ExperimentalAtomicApi::class)
        fun containsHash(key: ByteString): Boolean {
            val segmentIndex = key[1].toInt() and 0x0F
            val list = segments.loadAt(segmentIndex)
            val idx = list.findKeyIndex(key)
            return idx >= 0
        }

        private fun List<Entry>.findKeyIndex(key: ByteString): Int {
            var low = 0
            var high = size - 1

            while (low <= high) {
                val mid = (low + high).ushr(1)
                val aBytes = get(mid).hash

                var cmp: Int
                val a1 = aBytes[1]
                val b1 = key[1]
                cmp = (a1.toInt() and 0xFF) - (b1.toInt() and 0xFF)
                if (cmp == 0) {
                    for (i in 2 until 32) {
                        val a = aBytes[i]
                        val b = key[i]
                        if (a != b) {
                            cmp = (a.toInt() and 0xFF) - (b.toInt() and 0xFF)
                            break
                        }
                    }
                }

                if (cmp < 0) {
                    low = mid + 1
                } else if (cmp > 0) {
                    high = mid - 1
                } else {
                    return mid
                }
            }

            return -(low + 1)
        }
    }

    private inner class Entry(
        val bucket: Bucket,
        val hash: ByteString,
        override val key: K,
        override var value: V
    ) : MutableMap.MutableEntry<K, V> {
        override fun setValue(newValue: V): V {
            val old = value
            value = newValue
            bucket.putHashedValue(hash, key, newValue)
            return old
        }

        override fun equals(other: Any?): Boolean =
            other is Map.Entry<*, *> && other.key == key && other.value == value

        override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    }
}
