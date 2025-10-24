@file:UseSerializers(
    ExtraCurrencyCollectionSerializer::class,
    CoinsSerializer::class,
    HashBytesAsBase64Serializer::class,
    TransactionDescriptionSerializer::class,
    AddressStdAsBase64Serializer::class
)

package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.crypto.HashBytes
import org.ton.sdk.blockchain.account.AccountStatus
import org.ton.sdk.blockchain.address.AddressStd
import org.ton.sdk.blockchain.currency.Coins
import org.ton.sdk.blockchain.currency.ExtraCurrencyCollection
import org.ton.sdk.blockchain.transaction.TransactionDescription
import org.ton.sdk.toncenter.internal.serializers.*

@Serializable
public class TonCenterTransaction(
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
    public val totalFeesExtraCurrencies: ExtraCurrencyCollection,
    public val description: TransactionDescription,
    public val blockRef: TonCenterBlockId,
    public val inMsg: TonCenterMessage?,
    public val outMsgs: List<TonCenterMessage>,
    public val accountStateBefore: TonCenterAccountState,
    public val accountStateAfter: TonCenterAccountState,
    public val emulated: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TonCenterTransaction) return false
        if (lt != other.lt) return false
        if (now != other.now) return false
        if (mcBlockSeqno != other.mcBlockSeqno) return false
        if (prevTransLt != other.prevTransLt) return false
        if (emulated != other.emulated) return false
        if (account != other.account) return false
        if (hash != other.hash) return false
        if (traceId != other.traceId) return false
        if (prevTransHash != other.prevTransHash) return false
        if (origStatus != other.origStatus) return false
        if (endStatus != other.endStatus) return false
        if (totalFees != other.totalFees) return false
        if (totalFeesExtraCurrencies != other.totalFeesExtraCurrencies) return false
        if (description != other.description) return false
        if (blockRef != other.blockRef) return false
        if (inMsg != other.inMsg) return false
        if (outMsgs != other.outMsgs) return false
        if (accountStateBefore != other.accountStateBefore) return false
        if (accountStateAfter != other.accountStateAfter) return false
        return true
    }

    override fun hashCode(): Int {
        var result = lt.hashCode()
        result = 31 * result + now.hashCode()
        result = 31 * result + mcBlockSeqno
        result = 31 * result + prevTransLt.hashCode()
        result = 31 * result + emulated.hashCode()
        result = 31 * result + account.hashCode()
        result = 31 * result + hash.hashCode()
        result = 31 * result + (traceId?.hashCode() ?: 0)
        result = 31 * result + prevTransHash.hashCode()
        result = 31 * result + origStatus.hashCode()
        result = 31 * result + endStatus.hashCode()
        result = 31 * result + totalFees.hashCode()
        result = 31 * result + totalFeesExtraCurrencies.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + blockRef.hashCode()
        result = 31 * result + (inMsg?.hashCode() ?: 0)
        result = 31 * result + outMsgs.hashCode()
        result = 31 * result + accountStateBefore.hashCode()
        result = 31 * result + accountStateAfter.hashCode()
        return result
    }

    override fun toString(): String = buildString {
        append("TonCenterTransaction(account=").append(account)
        append(", hash=").append(hash)
        append(", lt=").append(lt)
        append(", now=").append(now)
        append(", mcBlockSeqno=").append(mcBlockSeqno)
        if (traceId != null) {
            append(", traceId=").append(traceId)
        }
        append(", prevTransHash=").append(prevTransHash)
        append(", prevTransLt=").append(prevTransLt)
        append(", origStatus=").append(origStatus)
        append(", endStatus=").append(endStatus)
        append(", totalFees=").append(totalFees)
        append(", totalFeesExtraCurrencies=").append(totalFeesExtraCurrencies)
        append(", description=").append(description)
        append(", blockRef=").append(blockRef)
        append(", inMsg=").append(inMsg)
        append(", outMsgs=").append(outMsgs)
        append(", accountStateBefore=").append(accountStateBefore)
        append(", accountStateAfter=").append(accountStateAfter)
        append(", emulated=").append(emulated)
        append(")")
    }
}
