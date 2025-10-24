@file:UseSerializers(
    HashBytesAsBase64Serializer::class,
    AddressStdAsBase64Serializer::class,
    CoinsSerializer::class,
    ExtraCurrencyCollectionSerializer::class,
)

package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.crypto.HashBytes
import org.ton.sdk.blockchain.address.AddressStd
import org.ton.sdk.blockchain.currency.Coins
import org.ton.sdk.blockchain.currency.ExtraCurrencyCollection
import org.ton.sdk.toncenter.internal.serializers.*

@Serializable
public class TonCenterMessage(
    public val hash: HashBytes,
    public val hashNorm: HashBytes? = null,
    public val source: AddressStd?,
    public val destination: AddressStd?,
    public val value: Coins?,
    public val valueExtraCurrencies: ExtraCurrencyCollection?,
    public val fwdFee: Coins?,
    public val ihrFee: Coins?,
    public val createdLt: Long?,
    public val createdAt: Long?,
    @Serializable(IntHexSerializer::class)
    public val opcode: Int?,
    public val decodedOpcode: String?,
    public val ihrDisabled: Boolean?,
    public val bounce: Boolean?,
    public val bounced: Boolean?,
    public val importFee: Coins?,
    public val inMsgTxHash: HashBytes? = null,
    public val outMsgTxHash: HashBytes? = null,
    public val messageContent: TonCenterMessageContent,
    public val initState: TonCenterMessageContent?,
    public val extraFlags: String? = null
)
