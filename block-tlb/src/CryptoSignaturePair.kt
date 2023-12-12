package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb

@Serializable
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
        storeBits(value.node_id_short)
        storeTlb(CryptoSignature, value.sign)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): CryptoSignaturePair = cellSlice {
        val nodeIdShort = loadBits(256)
        val sign = loadTlb(CryptoSignature)
        CryptoSignaturePair(nodeIdShort, sign)
    }
}
