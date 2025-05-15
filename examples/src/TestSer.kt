package org.ton.kotlin.examples

import org.ton.kotlin.block.AddrStd
import org.ton.kotlin.block.CommonMsgInfo
import org.ton.kotlin.block.ExtInMsgInfo
import org.ton.kotlin.cell.buildCell
import org.ton.kotlin.tlb.storeTlb

fun main() {
    val a = ExtInMsgInfo(
        null,
//        AddrStd(0, BitString("FF".repeat(32)))
        AddrStd("kQBZE6MtrDRmMYDNKJwQDDRc1UiiaMRH9XFfrbVQ53zkz2Yl")
    )
    val cell = buildCell { storeTlb(CommonMsgInfo, a) }
    println(cell.bits.toBinary())
}
