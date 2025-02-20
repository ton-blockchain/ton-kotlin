package org.ton.hashmap

import org.ton.bitstring.BitString
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.time.ExperimentalTime

class HashMapEditTest {

    @ExperimentalTime
    @Test
    fun test() {
        val list = List(100) {
            BitString(*BooleanArray(32) { Random.nextBoolean() })
        }
        val expected = list.sorted()

        repeat(10) {
            var hashMap = HashMapE.empty<BitString>()

            list.shuffled().forEach {
                hashMap = hashMap.set(it)
            }
            val actual = hashMap.map { it.first }
            assertContentEquals(expected, actual)
        }
    }
}

private fun HashMapE<BitString>.set(key: BitString): HmeRoot<BitString> {
    return set(key, key)
}

private fun HashMapE<BitString>.debug() {
    println("=================================")
    println((this as HmeRoot<*>).root.load())
    println("--------------")
    println(this.joinToString("\n") {
        "${it.first.toBinary()} = ${it.second}"
    })
}
