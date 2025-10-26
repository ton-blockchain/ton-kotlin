package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb


@SerialName("signed_certificate")
public data class SignedCertificate(
    val certificate: Certificate,
    val certificate_signature: CryptoSignature
) {
    public companion object : TlbConstructorProvider<SignedCertificate> by SignedCertificatedTlbConstructor
}

private object SignedCertificatedTlbConstructor : TlbConstructor<SignedCertificate>(
    schema = "signed_certificate\$_ certificate:Certificate certificate_signature:CryptoSignature\n" +
            "  = SignedCertificate;"
) {

    override fun storeTlb(
        builder: CellBuilder,
        value: SignedCertificate
    ) = builder {
        storeTlb(Certificate, value.certificate)
        storeTlb(CryptoSignature, value.certificate_signature)
    }

    override fun loadTlb(
        slice: CellSlice
    ): SignedCertificate = slice {
        val certificate = loadTlb(Certificate)
        val certificateSignature = loadTlb(CryptoSignature)
        SignedCertificate(certificate, certificateSignature)
    }
}
