package org.ton.proxy.dns

import io.ktor.utils.io.core.*
import org.ton.bigint.BigInt
import org.ton.kotlin.crypto.sha256.sha256

enum class DnsCategory(
    val value: BigInt
) {
    WALLET("wallet"),
    SITE("site");

    constructor(name: String) : this(BigInt(sha256(name.toByteArray())))
}
