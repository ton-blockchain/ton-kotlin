package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb

@Serializable
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
        cellBuilder: CellBuilder,
        value: SignedCertificate
    ) = cellBuilder {
        storeTlb(Certificate, value.certificate)
        storeTlb(CryptoSignature, value.certificate_signature)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): SignedCertificate = cellSlice {
        val certificate = loadTlb(Certificate)
        val certificateSignature = loadTlb(CryptoSignature)
        SignedCertificate(certificate, certificateSignature)
    }
}
