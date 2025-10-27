@file:UseSerializers(
    HashBytesAsBase64Serializer::class,
    AddressStdAsBase64Serializer::class,
    CoinsSerializer::class,
    ExtraCurrencyCollectionSerializer::class,
)

package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.sdk.blockchain.address.AddressStd
import org.ton.sdk.blockchain.currency.Coins
import org.ton.sdk.blockchain.currency.ExtraCurrencyCollection
import org.ton.sdk.crypto.HashBytes
import org.ton.sdk.toncenter.internal.serializers.*
import kotlin.jvm.JvmName

@Serializable
public class TonCenterMessage(
    @get:JvmName("hash") public val hash: HashBytes,
    @get:JvmName("hashNorm") public val hashNorm: HashBytes? = null,
    @get:JvmName("source") public val source: AddressStd?,
    @get:JvmName("destination") public val destination: AddressStd?,
    @get:JvmName("value") public val value: Coins?,
    @get:JvmName("valueExtraCurrencies") public val valueExtraCurrencies: ExtraCurrencyCollection?,
    @get:JvmName("fwdFee") public val fwdFee: Coins?,
    @get:JvmName("ihrFee") public val ihrFee: Coins?,
    @get:JvmName("createdLt") public val createdLt: Long?,
    @get:JvmName("createdAt") public val createdAt: Long?,
    @Serializable(IntHexSerializer::class)
    @get:JvmName("opcode") public val opcode: Int?,
    @get:JvmName("decodedOpcode") public val decodedOpcode: String?,
    @get:JvmName("ihrDisabled") public val ihrDisabled: Boolean?,
    @get:JvmName("bounce") public val bounce: Boolean?,
    @get:JvmName("bounced") public val bounced: Boolean?,
    @get:JvmName("importFee") public val importFee: Coins?,
    @get:JvmName("inMsgTxHash") public val inMsgTxHash: HashBytes? = null,
    @get:JvmName("outMsgTxHash") public val outMsgTxHash: HashBytes? = null,
    @get:JvmName("messageContent") public val messageContent: TonCenterMessageContent,
    @get:JvmName("initState") public val initState: TonCenterMessageContent?,
    @get:JvmName("extraFlags") public val extraFlags: String? = null
)
