@file:Suppress("OPT_IN_USAGE")

package org.ton.api.pk

import kotlinx.serialization.Serializable
import org.ton.api.adnl.AdnlIdShort
import org.ton.api.pub.PublicKey
import org.ton.crypto.Decryptor
import org.ton.tl.TlCombinator

@Serializable
public sealed interface PrivateKey : Decryptor {
    public fun publicKey(): PublicKey
    public fun toAdnlIdShort(): AdnlIdShort = publicKey().toAdnlIdShort()

    public companion object : TlCombinator<PrivateKey>(
        PrivateKey::class,
        PrivateKeyUnencrypted::class to PrivateKeyUnencrypted,
        PrivateKeyEd25519::class to PrivateKeyEd25519.tlConstructor(),
        PrivateKeyAes::class to PrivateKeyAes,
        PrivateKeyOverlay::class to PrivateKeyOverlay,
    )
}
