package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb


@SerialName("chained_signature")
public data class ChainedSignature(
    val signed_crt: SignedCertificate,
    val temp_key_signature: CryptoSignatureSimple
) : CryptoSignature {
    public companion object : TlbConstructorProvider<ChainedSignature> by ChainedSignatureTLbConstructor
}

private object ChainedSignatureTLbConstructor : TlbConstructor<ChainedSignature>(
    schema = "chained_signature#f signed_cert:^SignedCertificate temp_key_signature:CryptoSignatureSimple = CryptoSignature;"
) {

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: ChainedSignature
    ) = cellBuilder {
        storeRef {
            storeTlb(SignedCertificate, value.signed_crt)
        }
        storeTlb(CryptoSignatureSimple, value.temp_key_signature)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): ChainedSignature = cellSlice {
        val signedCrt = loadRef {
            loadTlb(SignedCertificate)
        }
        val tempKetSignature = loadTlb(CryptoSignatureSimple)
        ChainedSignature(signedCrt, tempKetSignature)
    }
}
