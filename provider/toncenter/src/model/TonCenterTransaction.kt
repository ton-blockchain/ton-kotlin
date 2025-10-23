package org.ton.kotlin.provider.toncenter.model

import kotlinx.serialization.Serializer
import org.ton.kotlin.blockchain.account.AccountStatus
import org.ton.kotlin.blockchain.currency.Coins
import org.ton.kotlin.blockchain.currency.ExtraCurrencyCollection
import org.ton.kotlin.blockchain.message.address.AddressStd
import org.ton.kotlin.crypto.HashBytes
import org.ton.kotlin.provider.toncenter.internal.serializers.ExtraCurrencyCollectionSerializer

@Serializer
public class TonCenterTransaction internal constructor(
    public val account: AddressStd,
    public val hash: HashBytes,
    public val lt: Long,
    public val now: Long,
    public val mcBlockSeqno: Int,
    public val traceId: HashBytes? = null,
    public val prevTransHash: HashBytes,
    public val prevTransLt: Long,
    public val origStatus: AccountStatus,
    public val endStatus: AccountStatus,
    public val totalFees: Coins,
    @Serializer(ExtraCurrencyCollectionSerializer::class)
    public val totalFeesExtraCurrencies: ExtraCurrencyCollection,
)
