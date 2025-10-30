package org.ton.sdk.toncenter.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmName

@Serializable
public class TonCenterDecodedContent(
    @SerialName("@type")
    @get:JvmName("type")
    public val type: String,
    @get:JvmName("comment")
    public val comment: String? = null
)
