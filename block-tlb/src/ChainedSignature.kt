package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.*
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb


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
        builder: CellBuilder,
        value: ChainedSignature
    ) = builder {
        storeRef {
            storeTlb(SignedCertificate, value.signed_crt)
        }
        storeTlb(CryptoSignatureSimple, value.temp_key_signature)
    }

    override fun loadTlb(
        slice: CellSlice
    ): ChainedSignature = slice {
        val signedCrt = loadRef {
            loadTlb(SignedCertificate)
        }
        val tempKetSignature = loadTlb(CryptoSignatureSimple)
        ChainedSignature(signedCrt, tempKetSignature)
    }
}
