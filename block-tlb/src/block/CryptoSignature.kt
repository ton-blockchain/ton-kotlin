@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.block

import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.tlb.TlbCombinator
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider


@JsonClassDiscriminator("@type")
public sealed interface CryptoSignature {
    public companion object : TlbCombinatorProvider<CryptoSignature> by CryptoSignatureTlbCombinator
}

private object CryptoSignatureTlbCombinator : TlbCombinator<CryptoSignature>(
    CryptoSignature::class,
    CryptoSignatureSimple::class to CryptoSignatureSimple,
    ChainedSignature::class to ChainedSignature
)
