package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb


@SerialName("certificate")
public data class Certificate(
    val temp_key: SigPubKey,
    val valid_since: UInt,
    val valid_until: UInt
) {
    public companion object : TlbConstructorProvider<Certificate> by CertificateTlbConstructor
}

private object CertificateTlbConstructor : TlbConstructor<Certificate>(
    schema = "certificate#4 temp_key:SigPubKey valid_since:uint32 valid_until:uint32 = Certificate;"
) {

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: Certificate
    ) = cellBuilder {
        storeTlb(SigPubKey, value.temp_key)
        storeUInt32(value.valid_since)
        storeUInt32(value.valid_until)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): Certificate = cellSlice {
        val tempKey = loadTlb(SigPubKey)
        val validSince = loadUInt32()
        val validUntil = loadUInt32()
        Certificate(tempKey, validSince, validUntil)
    }
}
