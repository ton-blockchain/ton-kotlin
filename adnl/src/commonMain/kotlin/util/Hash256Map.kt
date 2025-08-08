package org.ton.kotlin.adnl.util

import kotlinx.io.bytestring.ByteString
import kotlin.concurrent.atomics.AtomicArray
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class Hash256Map<T> : AbstractMutableMap<ByteString, T>() {
    private val buckets = Array<Bucket<T>>(256) { Bucket() }

    override val entries: MutableSet<MutableMap.MutableEntry<ByteString, T>> =
        object : MutableSet<MutableMap.MutableEntry<ByteString, T>> {
            override val size: Int
                get() = this@Hash256Map.size

            override fun add(element: MutableMap.MutableEntry<ByteString, T>): Boolean {
                val exists = contains(element)
                put(element.key, element.value)
                return !exists
            }

            override fun addAll(elements: Collection<MutableMap.MutableEntry<ByteString, T>>): Boolean {
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
            override fun iterator(): MutableIterator<MutableMap.MutableEntry<ByteString, T>> {
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

                return object : MutableIterator<MutableMap.MutableEntry<ByteString, T>> {
                    private var current: MutableMap.MutableEntry<ByteString, T>? = null

                    override fun hasNext(): Boolean = iter.hasNext()

                    override fun next(): MutableMap.MutableEntry<ByteString, T> {
                        val next = iter.next()
                        current = next
                        return next
                    }

                    override fun remove() {
                        current?.let { this@Hash256Map.remove(it.key) }
                    }
                }
            }

            override fun contains(element: MutableMap.MutableEntry<ByteString, T>): Boolean {
                val v = get(element.key)
                return v == element.value
            }

            override fun containsAll(elements: Collection<MutableMap.MutableEntry<ByteString, T>>): Boolean {
                return elements.all { contains(it) }
            }

            override fun isEmpty(): Boolean = this@Hash256Map.isEmpty()

            override fun remove(element: MutableMap.MutableEntry<ByteString, T>): Boolean {
                val v = get(element.key)
                if (v == element.value) {
                    this@Hash256Map.remove(element.key)
                    return true
                }
                return false
            }

            override fun removeAll(elements: Collection<MutableMap.MutableEntry<ByteString, T>>): Boolean {
                var changed = false
                for (e in elements) {
                    changed = changed or remove(e)
                }
                return changed
            }

            override fun retainAll(elements: Collection<MutableMap.MutableEntry<ByteString, T>>): Boolean {
                val toKeep = elements.map { it.key }.toSet()
                val toRemove = this@Hash256Map.keys.filter { it !in toKeep }
                for (k in toRemove) {
                    this@Hash256Map.remove(k)
                }
                return toRemove.isNotEmpty()
            }
        }


    override fun put(key: ByteString, value: T): T? {
        return buckets[key[0].toInt() and 0xFF].put(key, value)
    }

    override fun get(key: ByteString): T? {
        return buckets[key[0].toInt() and 0xFF].get(key)
    }

    override fun containsKey(key: ByteString): Boolean {
        return buckets[key[0].toInt() and 0xFF].containsKey(key)
    }

    override fun remove(key: ByteString): T? {
        return buckets[key[0].toInt() and 0xFF].remove(key)
    }

    override fun clear() {
        for (bucket in buckets) {
            bucket.clear()
        }
    }

    override val size: Int
        get() = buckets.sumOf { it.size }

    private class Bucket<T>() {
        @OptIn(ExperimentalAtomicApi::class)
        val segments = AtomicArray<List<Entry<T>>>(16) {
            emptyList<Entry<T>>()
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
        fun put(key: ByteString, value: T): T? {
            val segmentIdx = key[1].toInt() and 0x0F
            while (true) {
                val oldList = segments.loadAt(segmentIdx)
                val idx = oldList.findKeyIndex(key)
                val newList: List<Entry<T>>
                val result: T?
                if (idx >= 0) {
                    result = oldList[idx].value
                    newList = ArrayList(oldList)
                    newList[idx] = Entry(this, key, value)
                } else {
                    result = null
                    newList = ArrayList(oldList.size + 1)
                    newList.addAll(oldList)
                    newList.add(-idx - 1, Entry(this, key, value))
                }
                if (segments.compareAndSetAt(segmentIdx, oldList, newList)) {
                    return result
                }
            }
        }

        @OptIn(ExperimentalAtomicApi::class)
        fun remove(key: ByteString): T? {
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
        fun get(key: ByteString): T? {
            val segmentIndex = key[1].toInt() and 0x0F
            val list = segments.loadAt(segmentIndex)
            val idx = list.findKeyIndex(key)
            return if (idx >= 0) {
                list[idx].value
            } else {
                null
            }
        }

        @OptIn(ExperimentalAtomicApi::class)
        fun containsKey(key: ByteString): Boolean {
            val segmentIndex = key[1].toInt() and 0x0F
            val list = segments.loadAt(segmentIndex)
            val idx = list.findKeyIndex(key)
            return idx >= 0
        }

        private fun List<Entry<T>>.findKeyIndex(key: ByteString): Int {
            var low = 0
            var high = size - 1

            while (low <= high) {
                val mid = (low + high).ushr(1)
                val aBytes = get(mid).key

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

    private class Entry<V>(
        val bucket: Bucket<V>,
        override val key: ByteString,
        override var value: V
    ) : MutableMap.MutableEntry<ByteString, V> {
        override fun setValue(newValue: V): V {
            val old = value
            value = newValue
            bucket.put(key, newValue)
            return old
        }

        override fun equals(other: Any?): Boolean =
            other is Map.Entry<*, *> && other.key == key && other.value == value

        override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    }
}
