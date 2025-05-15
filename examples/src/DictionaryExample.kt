package org.ton.kotlin.examples

import io.ktor.util.*
import org.ton.boc.BagOfCells
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.buildCell
import org.ton.kotlin.dict.RawDictionary

fun main() {
    val dict = RawDictionary(32)
    dict[BitString("00000000")] = buildCell {
        storeInt(42, 32)
        storeInt(1337, 32)
    }.beginParse()

    println(BagOfCells(dict.root!!).toByteArray().encodeBase64())
}
