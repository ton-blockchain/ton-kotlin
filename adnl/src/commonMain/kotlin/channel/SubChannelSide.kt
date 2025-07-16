package org.ton.kotlin.adnl.channel

import org.ton.kotlin.adnl.AdnlIdShort
import org.ton.kotlin.adnl.AdnlPeerPair
import org.ton.kotlin.crypto.*

class AdnlChannel(
    val remoteKey: PublicKeyEd25519,
    val inId: AdnlIdShort,
    val outId: AdnlIdShort,
    val encryptor: EncryptorAes,
    val decryptor: DecryptorAes,
    var isReady: Boolean = false,
) {
    companion object {
        fun create(
            peerPair: AdnlPeerPair,
            localKey: PrivateKeyEd25519,
            remoteKey: PublicKeyEd25519,
            isReady: Boolean = false
        ): AdnlChannel {
            val sharedSecret = localKey.computeSharedSecret(remoteKey)
            val decryptSecret: PrivateKeyAes
            val encryptSecret: PublicKeyAes
            val compared = peerPair.localId.compareTo(peerPair.remoteId)
            if (compared == 0) {
                decryptSecret = PrivateKeyAes(sharedSecret)
                encryptSecret = PublicKeyAes(sharedSecret)
            } else {
                val reversedSecret = sharedSecret.reversedArray()
                if (compared < 0) {
                    decryptSecret = PrivateKeyAes(sharedSecret)
                    encryptSecret = PublicKeyAes(reversedSecret)
                } else {
                    decryptSecret = PrivateKeyAes(reversedSecret)
                    encryptSecret = PublicKeyAes(sharedSecret)
                }
            }
            return AdnlChannel(
                remoteKey,
                AdnlIdShort(decryptSecret.computeShortId()),
                AdnlIdShort(encryptSecret.computeShortId()),
                encryptSecret.createEncryptor(),
                decryptSecret.createDecryptor(),
                isReady
            )
        }

        private fun prioritySecret(
            ordinarySecret: ByteArray
        ) = byteArrayOf(
            ordinarySecret[1], ordinarySecret[0], ordinarySecret[3], ordinarySecret[2],
            ordinarySecret[5], ordinarySecret[4], ordinarySecret[7], ordinarySecret[6],
            ordinarySecret[9], ordinarySecret[8], ordinarySecret[11], ordinarySecret[10],
            ordinarySecret[13], ordinarySecret[12], ordinarySecret[15], ordinarySecret[14],
            ordinarySecret[17], ordinarySecret[16], ordinarySecret[19], ordinarySecret[18],
            ordinarySecret[21], ordinarySecret[20], ordinarySecret[23], ordinarySecret[22],
            ordinarySecret[25], ordinarySecret[24], ordinarySecret[27], ordinarySecret[26],
            ordinarySecret[29], ordinarySecret[28], ordinarySecret[31], ordinarySecret[30]
        )
    }
}
