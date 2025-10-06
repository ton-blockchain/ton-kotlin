@file:Suppress("OPT_IN_USAGE")

package org.ton.api.pk

import kotlinx.serialization.Serializable
import org.ton.api.adnl.AdnlIdShort
import org.ton.api.pub.PublicKey
import org.ton.kotlin.crypto.Decryptor
import org.ton.kotlin.tl.TlCombinator

@Serializable
public sealed interface PrivateKey : Decryptor {
    public fun publicKey(): PublicKey
    public fun toAdnlIdShort(): AdnlIdShort = publicKey().toAdnlIdShort()

    public companion object : TlCombinator<PrivateKey>(
        PrivateKey::class,
        PrivateKeyUnencrypted::class to PrivateKeyUnencrypted,
        PrivateKeyEd25519::class to PrivateKeyEd25519,
        PrivateKeyAes::class to PrivateKeyAes,
        PrivateKeyOverlay::class to PrivateKeyOverlay,
    )
}
