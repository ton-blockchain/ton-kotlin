package org.ton.kotlin.provider.toncenter.model

import kotlinx.serialization.Serializable

@Serializable
public data class TonCenterTokenInfo(
    val valid: Boolean,
    val type: String,
    val name: String,
    val symbol: String,
    val description: String,
    val image: String,
    val extra: Map<String, String>
) {
    public enum class Type {
        JETTON_MASTER,
        JETTON_WALLET,
        NFT_COLLECTION,
        NFT_ITEM
    }
}
