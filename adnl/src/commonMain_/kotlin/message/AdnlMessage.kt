package org.ton.kotlin.adnl.message

import org.ton.kotlin.crypto.PublicKeyEd25519
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
sealed interface AdnlMessage {

    class CreateChannel(
        val localKey: PublicKeyEd25519,
        val date: Instant,
    ) : AdnlMessage

    class ConfirmChannel(
        val localKey: PublicKeyEd25519,
        val remoteKey: PublicKeyEd25519,
        val date: Instant,
    ) : AdnlMessage

}
