package org.ton.block

import org.ton.crypto.hex
import kotlin.test.Test

class AddrStdTest {
    @Test
    fun testParse() {
        val original = "EQAKtVj024T9MfYaJzU1xnDAkf_GGbHNu-V2mgvyjTuP6rvC"
        val addrStd = AddrStd(original)
        println(addrStd)
        val addrStd1 = AddrStd(0, hex("0AB558F4DB84FD31F61A273535C670C091FFC619B1CDBBE5769A0BF28D3B8FEA"))
        println(original)
        println(addrStd1.toString(userFriendly = true))
    }
}
