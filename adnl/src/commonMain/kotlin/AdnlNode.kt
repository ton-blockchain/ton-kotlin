package org.ton.kotlin.adnl

data class AdnlNode(
    val publicId: AdnlIdFull,
    val addresses: AddressList
) {
    val peerId get() = publicId.idShort
    val publicKey get() = publicId.publicKey
}
