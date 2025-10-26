package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb

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
        builder: CellBuilder,
        value: DnsNextResolver
    ) {
        builder.storeTlb(MsgAddressInt, value.resolver)
    }

    override fun loadTlb(
        slice: CellSlice
    ): DnsNextResolver {
        val resolver = slice.loadTlb(MsgAddressInt)
        return DnsNextResolver(resolver)
    }
}
