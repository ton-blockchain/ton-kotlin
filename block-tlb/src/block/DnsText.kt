package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb


public class DnsText(
    @SerialName("_")
    public val value: Text
) : DnsRecord {
    public companion object : TlbConstructorProvider<DnsText> by DnsTextTlbConstructor
}

private object DnsTextTlbConstructor : TlbConstructor<DnsText>(
    schema = "dns_text#1eda _:Text = DNSRecord;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: DnsText) {
        cellBuilder.storeTlb(Text, value.value)
    }

    override fun loadTlb(cellSlice: CellSlice): DnsText {
        val value = cellSlice.loadTlb(Text)
        return DnsText(value)
    }
}
