@file:UseSerializers(
    AddressStdAsBase64Serializer::class,
    ExtraCurrencyCollectionSerializer::class,
    CoinsSerializer::class,
    HashBytesAsBase64Serializer::class
)

package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.sdk.blockchain.address.AddressStd
import org.ton.sdk.blockchain.currency.Coins
import org.ton.sdk.blockchain.currency.ExtraCurrencyCollection
import org.ton.sdk.crypto.HashBytes
import org.ton.sdk.toncenter.internal.serializers.AddressStdAsBase64Serializer
import org.ton.sdk.toncenter.internal.serializers.CoinsSerializer
import org.ton.sdk.toncenter.internal.serializers.ExtraCurrencyCollectionSerializer
import org.ton.sdk.toncenter.internal.serializers.HashBytesAsBase64Serializer

@Serializable
public class TonCenterWalletState(
    public val address: AddressStd,
    public val isWallet: Boolean,
    public val walletType: String? = null,
    public val seqno: Int? = null,
    public val walletId: Int? = null,
    public val balance: Coins? = null,
    public val extraCurrencies: ExtraCurrencyCollection? = null,
    public val isSignatureAllowed: Boolean? = null,
    public val status: String? = null,
    public val codeHash: HashBytes? = null,
    public val lastTransactionHash: HashBytes,
    public val lastTransactionLt: Long
)
