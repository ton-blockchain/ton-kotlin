package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb

@SerialName("dns_next_resolver")

public data class DnsNextResolver(
    val resolver: MsgAddressInt
) : DnsRecord {
    public companion object : TlbConstructorProvider<DnsNextResolver> by DNSNextResolverTlbConstructor
}

private object DNSNextResolverTlbConstructor : TlbConstructor<DnsNextResolver>(
    schema = "dns_next_resolver#ba93 resolver:MsgAddressInt = DNSNextResolver;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: DnsNextResolver
    ) {
        cellBuilder.storeTlb(MsgAddressInt, value.resolver)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): DnsNextResolver {
        val resolver = cellSlice.loadTlb(MsgAddressInt)
        return DnsNextResolver(resolver)
    }
}
