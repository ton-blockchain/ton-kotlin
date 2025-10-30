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
import kotlin.jvm.JvmName

@Serializable
public class TonCenterWalletState(
    @get:JvmName("address") public val address: AddressStd,
    @get:JvmName("isWallet") public val isWallet: Boolean,
    @get:JvmName("walletType") public val walletType: String? = null,
    @get:JvmName("seqno") public val seqno: Int? = null,
    @get:JvmName("walletId") public val walletId: Int? = null,
    @get:JvmName("balance") public val balance: Coins? = null,
    @get:JvmName("extraCurrencies") public val extraCurrencies: ExtraCurrencyCollection? = null,
    @get:JvmName("isSignatureAllowed") public val isSignatureAllowed: Boolean? = null,
    @get:JvmName("status") public val status: String? = null,
    @get:JvmName("codeHash") public val codeHash: HashBytes? = null,
    @get:JvmName("lastTransactionHash") public val lastTransactionHash: HashBytes,
    @get:JvmName("lastTransactionLt") public val lastTransactionLt: Long
)
