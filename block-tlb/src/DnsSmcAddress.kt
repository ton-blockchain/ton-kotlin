package org.ton.kotlin.block

import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb

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
    override fun storeTlb(cellBuilder: CellBuilder, value: DnsSmcAddress) {
        cellBuilder.storeTlb(MsgAddressInt, value.smc_address)
        cellBuilder.storeBitString(value.flags)
        if (value.flags[0]) {
            cellBuilder.storeTlb(SmcCapList, value.cap_list!!)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): DnsSmcAddress {
        val smc_address = cellSlice.loadTlb(MsgAddressInt)
        val flags = cellSlice.loadBitString(8)
        val cap_list = if (flags[0]) cellSlice.loadTlb(SmcCapList) else null
        return DnsSmcAddress(smc_address, flags, cap_list)
    }
}
