package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb


@SerialName("sig_pair")
public data class CryptoSignaturePair(
    val node_id_short: BitString,
    val sign: CryptoSignature
) {
    public companion object : TlbConstructorProvider<CryptoSignaturePair> by CryptoSignaturePairTlbConstructor
}

private object CryptoSignaturePairTlbConstructor : TlbConstructor<CryptoSignaturePair>(
    schema = "sig_pair\$_ node_id_short:bits256 sign:CryptoSignature = CryptoSignaturePair;  // 256+x ~ 772 bits\n"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: CryptoSignaturePair
    ) = cellBuilder {
        storeBitString(value.node_id_short)
        storeTlb(CryptoSignature, value.sign)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): CryptoSignaturePair = cellSlice {
        val nodeIdShort = loadBitString(256)
        val sign = loadTlb(CryptoSignature)
        CryptoSignaturePair(nodeIdShort, sign)
    }
}
