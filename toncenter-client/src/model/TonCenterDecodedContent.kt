package org.ton.sdk.toncenter.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public class TonCenterDecodedContent(
    @SerialName("@type")
    public val type: String,
    public val comment: String? = null
)
