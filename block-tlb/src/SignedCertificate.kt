package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb


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
