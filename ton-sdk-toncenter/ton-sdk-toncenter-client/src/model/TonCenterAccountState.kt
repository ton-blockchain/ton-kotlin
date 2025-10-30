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
import org.ton.sdk.blockchain.account.AccountStatus
import org.ton.sdk.blockchain.currency.Coins
import org.ton.sdk.blockchain.currency.ExtraCurrencyCollection
import org.ton.sdk.crypto.HashBytes
import org.ton.sdk.tl.serializers.ByteStringBase64Serializer
import org.ton.sdk.toncenter.internal.serializers.CoinsSerializer
import org.ton.sdk.toncenter.internal.serializers.ExtraCurrencyCollectionSerializer
import org.ton.sdk.toncenter.internal.serializers.HashBytesAsBase64Serializer
import kotlin.jvm.JvmName

@Serializable
public class TonCenterAccountState(
    @get:JvmName("hash") public val hash: HashBytes,
    @get:JvmName("balance") public val balance: Coins?,
    @get:JvmName("extraCurrencies") public val extraCurrencies: ExtraCurrencyCollection?,
    @get:JvmName("accountStatus") public val accountStatus: AccountStatus?,
    @get:JvmName("frozenHash") public val frozenHash: HashBytes?,
    @get:JvmName("dataHash") public val dataHash: HashBytes?,
    @get:JvmName("codeHash") public val codeHash: HashBytes?,
    @get:JvmName("dataBoc") public val dataBoc: ByteString? = null,
    @get:JvmName("codeBoc") public val codeBoc: ByteString? = null
)
