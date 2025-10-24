@file:UseSerializers(
    CoinsSerializer::class,
    ExtraCurrencyCollectionSerializer::class,
    HashBytesAsBase64Serializer::class,
    ByteStringBase64Serializer::class
)

package org.ton.sdk.toncenter.model

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.crypto.HashBytes
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer
import org.ton.sdk.blockchain.account.AccountStatus
import org.ton.sdk.blockchain.currency.Coins
import org.ton.sdk.blockchain.currency.ExtraCurrencyCollection
import org.ton.sdk.toncenter.internal.serializers.CoinsSerializer
import org.ton.sdk.toncenter.internal.serializers.ExtraCurrencyCollectionSerializer
import org.ton.sdk.toncenter.internal.serializers.HashBytesAsBase64Serializer

@Serializable
public class TonCenterAccountState(
    public val hash: HashBytes,
    public val balance: Coins?,
    public val extraCurrencies: ExtraCurrencyCollection?,
    public val accountStatus: AccountStatus?,
    public val frozenHash: HashBytes?,
    public val dataHash: HashBytes?,
    public val codeHash: HashBytes?,
    public val dataBoc: ByteString? = null,
    public val codeBoc: ByteString? = null
)
