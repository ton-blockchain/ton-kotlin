package org.ton.kotlin.examples.contract.config

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.account.ShardAccount
import org.ton.kotlin.adnl.tonnode.TonNodeBlockIdExt
import org.ton.kotlin.block.AddrStd
import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.dict.Dictionary
import org.ton.kotlin.dict.DictionaryKeyCodec
import org.ton.kotlin.examples.provider.Provider
import org.ton.kotlin.tlb.CellRef
import org.ton.kotlin.tlb.TlbCodec

class ConfigContract(
    val provider: Provider
) {
    @OptIn(ExperimentalStdlibApi::class)
    val address = AddrStd(-1, "5555555555555555555555555555555555555555555555555555555555555555".hexToByteArray())

    suspend fun getState(blockId: TonNodeBlockIdExt? = null): ShardAccount? {
        return provider.getAccount(address, blockId)
    }
}

data class ConfigData(
    val cfgDict: Cell,
    val seqno: Int,
    val publicKey: ByteString,
    val voteDict: Dictionary<ByteString, ConfigProposalStatus>,
) {
    companion object : TlbCodec<ConfigData> by ConfigDataTlbCodec
}

private object ConfigDataTlbCodec : TlbCodec<ConfigData> {
    override fun loadTlb(slice: CellSlice, context: CellContext): ConfigData {
        val cfgDict = slice.loadRef()
        val seqno = slice.loadUInt(32).toInt()
        val publicKey = slice.loadByteString(32)
        val voteDict = Dictionary(slice.loadNullableRef(), DictionaryKeyCodec.BYTE_STRING_32, ConfigProposalStatus)
        return ConfigData(cfgDict, seqno, publicKey, voteDict)
    }
}

/**
 * ```tlb
 * cfg_proposal_status#ce expires:uint32 proposal:^ConfigProposal is_critical:Bool
 *   voters:(HashmapE 16 True) remaining_weight:int64 validator_set_id:uint256
 *   rounds_remaining:uint8 wins:uint8 losses:uint8 = ConfigProposalStatus;
 * ```
 */
data class ConfigProposalStatus(
    val expiresAt: Long,
    val proposal: CellRef<ConfigProposal>,
    val isCritical: Boolean,
    val voters: Dictionary<Int, Unit>,
    val remainingWeight: Long,
    val validatorSetId: ByteString,
    val roundsRemaining: Int,
    val wins: Int,
    val losses: Int
) {
    companion object : TlbCodec<ConfigProposalStatus> by ConfigProposalStatusTlbCodec
}

private object ConfigProposalStatusTlbCodec : TlbCodec<ConfigProposalStatus> {
    val key = DictionaryKeyCodec.int(16)
    val trueValue = object : TlbCodec<Unit> {
        override fun loadTlb(slice: CellSlice, context: CellContext) = Unit
        override fun storeTlb(builder: CellBuilder, value: Unit, context: CellContext) {
        }
    }

    override fun storeTlb(builder: CellBuilder, value: ConfigProposalStatus, context: CellContext) {
        builder.storeUInt(0xCE, 8)
        builder.storeUInt(value.expiresAt, 32)
        builder.storeRef(value.proposal.cell)
        builder.storeBoolean(value.isCritical)
        builder.storeNullableRef(value.voters.cell)
        builder.storeLong(value.remainingWeight, 64)
        builder.storeByteString(value.validatorSetId)
        builder.storeUInt(value.remainingWeight, 8)
        builder.storeUInt(value.wins, 8)
        builder.storeUInt(value.losses, 8)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun loadTlb(slice: CellSlice, context: CellContext): ConfigProposalStatus {
        val tag = slice.loadUInt(8).toInt()
        require(tag == 0xCE) { "Invalid ConfigProposalStatus tag: ${tag.toHexString()}" }
        val expiresAt = slice.loadUInt(32).toLong()
        val proposal = CellRef(slice.loadRef(), ConfigProposal)
        val isCritical = slice.loadBoolean()
        val voters = Dictionary(slice.loadNullableRef(), key, trueValue)
        val remainingWeight = slice.loadLong(64)
        val validatorSetId = slice.loadByteString(32)
        val roundsRemaining = slice.loadUInt(8).toInt()
        val wins = slice.loadUInt(8).toInt()
        val losses = slice.loadUInt(8).toInt()
        return ConfigProposalStatus(
            expiresAt,
            proposal,
            isCritical,
            voters,
            remainingWeight,
            validatorSetId,
            roundsRemaining,
            wins,
            losses
        )
    }
}

/**
 * ```tlb
 * cfg_proposal#f3 param_id:int32 param_value:(Maybe ^Cell) if_hash_equal:(Maybe uint256)
 * ```
 */
data class ConfigProposal(
    val paramId: Int,
    val paramValue: Cell?,
    val ifHashEqual: ByteString?,
) {
    companion object : TlbCodec<ConfigProposal> by ConfigProposalTlbCodec
}

private object ConfigProposalTlbCodec : TlbCodec<ConfigProposal> {
    @OptIn(ExperimentalStdlibApi::class)
    override fun loadTlb(slice: CellSlice, context: CellContext): ConfigProposal {
        val tag = slice.loadUInt(8).toInt()
        require(tag == 0xF3) { "Invalid ConfigProposal tag: ${tag.toHexString()}" }
        val paramId = slice.loadInt(32).toInt()
        val paramValue = slice.loadNullableRef()
        val ifHashEqual = if (slice.loadBoolean()) {
            slice.loadByteString(32)
        } else {
            null
        }
        return ConfigProposal(paramId, paramValue, ifHashEqual)
    }

    override fun storeTlb(builder: CellBuilder, value: ConfigProposal, context: CellContext) {
        builder.storeUInt(0xf3, 8)
        builder.storeInt(value.paramId, 32)
        builder.storeNullableRef(value.paramValue)
        if (value.ifHashEqual != null) {
            builder.storeBoolean(true)
            builder.storeByteString(value.ifHashEqual)
        } else {
            builder.storeBoolean(false)
        }
    }
}
