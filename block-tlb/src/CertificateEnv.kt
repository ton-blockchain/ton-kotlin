package org.ton.kotlin.block

import kotlinx.serialization.SerialName


@SerialName("certificate_env")
public data class CertificateEnv(
    val certificate: Certificate
)
