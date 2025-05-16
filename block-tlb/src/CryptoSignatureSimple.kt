package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


@SerialName("ed25519_signature")
public data class CryptoSignatureSimple(
    val r: BitString,
    val s: BitString
) : CryptoSignature {
    public companion object : TlbConstructorProvider<CryptoSignatureSimple> by CryptoSignatureSimpleTlbConstructor
}

private object CryptoSignatureSimpleTlbConstructor : TlbConstructor<CryptoSignatureSimple>(
    schema = "ed25519_signature#5 R:bits256 s:bits256 = CryptoSignatureSimple;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: CryptoSignatureSimple
    ) = cellBuilder {
        storeBitString(value.r)
        storeBitString(value.s)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): CryptoSignatureSimple = cellSlice {
        val r = loadBitString(256)
        val s = loadBitString(256)
        CryptoSignatureSimple(r, s)
    }
}
