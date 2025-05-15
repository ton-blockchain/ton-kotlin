package org.ton.kotlin.lite.client

import org.ton.boc.BagOfCells
import org.ton.kotlin.account.Account
import org.ton.kotlin.adnl.tonnode.TonNodeBlockIdExt
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.bitstring.toBitString
import org.ton.kotlin.block.*
import org.ton.kotlin.cell.Cell
import org.ton.kotlin.lite.client.internal.BlockHeaderResult
import org.ton.kotlin.lite.client.internal.FullAccountState
import org.ton.kotlin.lite.client.internal.TransactionId
import org.ton.kotlin.tlb.CellRef
import org.ton.kotlin.tlb.NullableTlbCodec

internal object CheckProofUtils {
    fun checkBlockHeaderProof(
        root: Cell,
        blockId: TonNodeBlockIdExt,
        storeStateHash: Boolean = false,
    ): BlockHeaderResult {
        val virtualHash = root.hash()
        check(virtualHash == blockId.rootHash.toByteArray().toBitString()) {
            "Invalid hash for block: $blockId, expected: ${blockId.rootHash}, actual: $virtualHash"
        }
        val block = Block.loadTlb(root)
        val time = block.info.value.genUtime.toInt()
        val lt = block.info.value.endLt.toLong()
        var stateHash: BitString? = null

        if (storeStateHash) {
            val stateUpdateCell = block.stateUpdate.toCell()
            stateHash = stateUpdateCell.refs[1].hash(0)
        }

        return BlockHeaderResult(time, lt, stateHash)
    }

    fun checkAccountProof(
        proof: ByteArray,
        shardBlock: TonNodeBlockIdExt,
        address: MsgAddressInt,
        root: Cell
    ): FullAccountState {
        val account = CellRef(root, NullableTlbCodec(Account))

        val qRoots = BagOfCells(proof).roots.toList()
        check(qRoots.size == 2) {
            "Invalid roots amount, expected: 2, actual: ${qRoots.size}"
        }

        val blockProofResult = checkBlockHeaderProof(MerkleProof.virtualize(qRoots[0]), shardBlock, true)

        val stateRoot = MerkleProof.virtualize(qRoots[1])
        val stateHash = stateRoot.hash()
        check(stateHash == blockProofResult.stateHash) {
            "Invalid state hash, expected: $stateHash, actual: ${blockProofResult.stateHash}"
        }

        val shardState = ShardState.loadTlb(stateRoot) as ShardStateUnsplit
        val shardAccount = checkNotNull(shardState.accounts.value.x[address.address]?.value) {
            "Shard account ${address.address} not found in shard state"
        }
        check(shardAccount.account.cell.virtualize().hash() == root.hash()) {
            "Account state hash mismatch, expected: ${shardAccount.account.hash()}, actual: ${root.hash()}"
        }

        return FullAccountState(
            shardBlock,
            address,
            TransactionId(shardAccount.lastTransHash.toByteArray(), shardAccount.lastTransLt.toLong()),
            account
        )
    }
}
