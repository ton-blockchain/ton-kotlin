package org.ton.kotlin.provider.toncenter.model

import kotlinx.serialization.Serializable
import org.ton.kotlin.blockchain.currency.Coins
import org.ton.kotlin.blockchain.currency.ExtraCurrencyCollection
import org.ton.kotlin.blockchain.message.address.AddressStd
import org.ton.kotlin.crypto.HashBytes
import org.ton.kotlin.provider.toncenter.internal.serializers.AddressStdAsBase64Serializer
import org.ton.kotlin.provider.toncenter.internal.serializers.ExtraCurrencyCollectionSerializer
import org.ton.kotlin.provider.toncenter.internal.serializers.HashBytesAsBase64Serializer

@Serializable
public data class TonCenterWalletState(
    @Serializable(AddressStdAsBase64Serializer::class)
    val address: AddressStd,
    val isWallet: Boolean,
    val walletType: String? = null,
    val seqno: Int? = null,
    val walletId: Int? = null,
    val balance: Coins? = null,
    @Serializable(with = ExtraCurrencyCollectionSerializer::class)
    val extraCurrencies: ExtraCurrencyCollection? = null,
    val isSignatureAllowed: Boolean? = null,
    val status: String? = null,
    @Serializable(with = HashBytesAsBase64Serializer::class)
    val codeHash: HashBytes? = null,
    @Serializable(with = HashBytesAsBase64Serializer::class)
    val lastTransactionHash: HashBytes,
    val lastTransactionLt: Long
)
