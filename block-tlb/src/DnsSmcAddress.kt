package org.ton.block

import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb

public data class DnsSmcAddress(
    val smc_address: MsgAddressInt,
    val flags: BitString,
    val cap_list: SmcCapList?
) : DnsRecord {
    public companion object : TlbConstructorProvider<DnsSmcAddress> by DnsSmcAddressTlbConstructor
}

private object DnsSmcAddressTlbConstructor : TlbConstructor<DnsSmcAddress>(
    schema = "dns_smc_address#9fd3 smc_address:MsgAddressInt flags:(## 8) cap_list:flags.0?SmcCapList = DNSRecord;"
) {
    override fun storeTlb(builder: CellBuilder, value: DnsSmcAddress) {
        builder.storeTlb(MsgAddressInt, value.smc_address)
        builder.storeBitString(value.flags)
        if (value.flags[0]) {
            builder.storeTlb(SmcCapList, value.cap_list!!)
        }
    }

    override fun loadTlb(slice: CellSlice): DnsSmcAddress {
        val smc_address = slice.loadTlb(MsgAddressInt)
        val flags = slice.loadBitString(8)
        val cap_list = if (flags[0]) slice.loadTlb(SmcCapList) else null
        return DnsSmcAddress(smc_address, flags, cap_list)
    }
}
