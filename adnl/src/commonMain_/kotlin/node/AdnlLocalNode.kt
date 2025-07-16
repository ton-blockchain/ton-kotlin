package org.ton.kotlin.adnl.node

import org.ton.kotlin.adnl.transport.AdnlTransport
import org.ton.kotlin.crypto.PrivateKeyEd25519

class AdnlLocalNode(
    val transport: AdnlTransport<*>,
    val localKey: PrivateKeyEd25519
) {
    val localId = localKey.computeShortId()
    val decryptor = localKey.createDecryptor()
}
