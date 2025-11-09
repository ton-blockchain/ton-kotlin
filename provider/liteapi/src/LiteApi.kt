@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.sdk.provider.liteapi

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.sdk.crypto.HashBytes
import org.ton.sdk.crypto.PublicKey
import org.ton.sdk.tl.TlConditional
import org.ton.sdk.tl.TlConstructorId
import org.ton.sdk.tl.serializers.ByteStringBase64Serializer

public interface LiteApi {
    @Serializable
    @SerialName("liteserver.desc")
    public data class LiteServerDesc(
        val id: PublicKey,
        val ip: Int,
        val port: Int
    ) {
        override fun toString(): String = "$ip:$port:$id"
    }

    /**
     * ```tl
     * tonNode.blockId#b7cdb167 workchain:int shard:long seqno:int = tonNode.BlockId;
     */
    @Serializable
    @TlConstructorId(0xb7cdb167)
    public class BlockId(
        public val workchain: Int,
        public val shard: Long,
        public val seqno: Int
    )

    /**
     * ```tl
     * tonNode.blockIdExt#6752eb78 workchain:int shard:long seqno:int root_hash:int256 file_hash:int256 = tonNode.BlockIdExt;
     */
    @Serializable
    @TlConstructorId(0x6752eb78)
    public class BlockIdExt(
        public val workchain: Int,
        public val shard: Long,
        public val seqno: Int,
        public val rootHash: HashBytes,
        public val fileHash: HashBytes
    )

    /**
     * ```tl
     * tonNode.zeroStateIdExt#1d7235ae workchain:int root_hash:int256 file_hash:int256 = tonNode.ZeroStateIdExt;
     */
    @Serializable
    @TlConstructorId(0x1d7235ae)
    public class ZeroStateIdExt(
        public val workchain: Int,
        public val rootHash: HashBytes,
        public val fileHash: HashBytes
    )

    /**
     * ```tl
     * liteServer.accountId#75a0e2c5 workchain:int id:int256 = liteServer.AccountId;
     */
    @Serializable
    @TlConstructorId(0x75a0e2c5)
    public class AccountId(
        public val workchain: Int,
        public val hash: HashBytes
    )

    /**
     * ```tl
     * liteServer.libraryEntry#8aff2446 hash:int256 data:bytes = liteServer.LibraryEntry;
     */
    @Serializable
    @TlConstructorId(0x8aff2446)
    public class LibraryEntry(
        public val hash: HashBytes,
        public val data: ByteString
    )

    /**
     * ```tl
     * liteServer.masterchainInfo#85832881 last:tonNode.blockIdExt state_root_hash:int256 init:tonNode.zeroStateIdExt = liteServer.MasterchainInfo;
     */
    @Serializable
    @TlConstructorId(0x85832881)
    public data class MasterchainInfo(
        val last: BlockIdExt,
        val stateRootHash: HashBytes,
        val init: ZeroStateIdExt,
    )

    /**
     * ```tl
     * liteServer.masterchainInfoExt#a8cce0f5 mode:# version:int capabilities:long last:tonNode.blockIdExt last_utime:int now:int state_root_hash:int256 init:tonNode.zeroStateIdExt = liteServer.MasterchainInfoExt;
     */
    @Serializable
    @TlConstructorId(0xa8cce0f5)
    public data class MasterchainInfoExt(
        val mode: Int,
        val version: Int,
        val capabilities: Long,
        val last: BlockIdExt,
        val lastUTime: Int,
        val now: Int,
        val stateRootHash: HashBytes,
        val init: ZeroStateIdExt,
    )

    /**
     * ```tl
     * liteServer.currentTime#e953000d now:int = liteServer.CurrentTime;
     */
    @Serializable
    @TlConstructorId(0xe953000d)
    public class CurrentTime(
        public val time: Int
    )

    /**
     * ```tl
     * liteServer.version#5a0491e5 mode:# version:int capabilities:long now:int = liteServer.Version;
     */
    @Serializable
    @TlConstructorId(0x5a0491e5)
    public class Version(
        public val mode: Int,
        public val version: Int,
        public val capabilities: Long
    )

    /**
     * ```tl
     * liteServer.blockData#a574ed6c id:tonNode.blockIdExt data:bytes = liteServer.BlockData;
     */
    @Serializable
    @TlConstructorId(0xa574ed6c)
    public class BlockData(
        public val id: BlockIdExt,
        public val data: ByteString
    )

    /**
     * ```tl
     * liteServer.blockState#abaddc0c id:tonNode.blockIdExt root_hash:int256 file_hash:int256 data:bytes = liteServer.BlockState;
     */
    @Serializable
    @TlConstructorId(0xabaddc0c)
    public class BlockState(
        public val rootHash: HashBytes,
        public val fileHash: HashBytes,
        public val data: ByteString
    )

    /**
     * ```tl
     * liteServer.blockHeader#752d8219 id:tonNode.blockIdExt mode:# header_proof:bytes = liteServer.BlockHeader;
     */
    @Serializable
    @TlConstructorId(0x752d8219)
    public class BlockHeader(
        public val id: BlockIdExt,
        public val mode: Int,
        public val headerProof: ByteString
    )

    /**
     * ```tl
     * liteServer.sendMsgStatus#3950e597 status:int = liteServer.SendMsgStatus;
     */
    @Serializable
    @TlConstructorId(0x3950e597)
    public class SendMsgStatus(
        public val status: Int
    )

    /**
     * ```tl
     * liteServer.accountState#7079c751 id:tonNode.blockIdExt shardblk:tonNode.blockIdExt shard_proof:bytes proof:bytes state:bytes = liteServer.AccountState;
     */
    @Serializable
    @TlConstructorId(0x7079c751)
    public class AccountState(
        public val id: BlockIdExt,
        public val shardBlock: BlockIdExt,
        public val shardProof: ByteString,
        public val proof: ByteString,
        public val state: ByteString
    )

    /**
     * ```tl
     * liteServer.runMethodResult#a39a616b mode:# id:tonNode.blockIdExt shardblk:tonNode.blockIdExt shard_proof:mode.0?bytes proof:mode.0?bytes state_proof:mode.1?bytes init_c7:mode.3?bytes lib_extras:mode.4?bytes exit_code:int result:mode.2?bytes = liteServer.RunMethodResult;
     */
    @Serializable
    @TlConstructorId(0xa39a616b)
    public class RunMethodResult(
        public val mode: Int,
        public val id: BlockIdExt,
        public val shardBlock: BlockIdExt,
        @TlConditional(field = "mode", mask = 1 shl 0)
        public val shardProof: ByteString?,
        @TlConditional(field = "mode", mask = 1 shl 0)
        public val proof: ByteString?,
        @TlConditional(field = "mode", mask = 1 shl 1)
        public val stateProof: ByteString?,
        @TlConditional(field = "mode", mask = 1 shl 3)
        public val initC7: ByteString?,
        @TlConditional(field = "mode", mask = 1 shl 4)
        public val libExtras: ByteString?,
        public val exitCode: Int,
        @TlConditional(field = "mode", mask = 1 shl 2)
        public val result: ByteString?
    )

    /**
     * ```tl
     * liteServer.shardInfo#9fe6cd84 id:tonNode.blockIdExt shardblk:tonNode.blockIdExt shard_proof:bytes shard_descr:bytes = liteServer.ShardInfo;
     */
    @Serializable
    @TlConstructorId(0x9fe6cd84)
    public class ShardInfo(
        public val id: BlockIdExt,
        public val shardBlock: BlockIdExt,
        public val shardProof: ByteString,
        public val shardDescr: ByteString
    )

    /**
     * ```tl
     * liteServer.allShardsInfo#098fe72d id:tonNode.blockIdExt proof:bytes data:bytes = liteServer.AllShardsInfo;
     */
    @Serializable
    @TlConstructorId(0x098fe72d)
    public class AllShardsInfo(
        public val id: BlockIdExt,
        public val proof: ByteString,
        public val data: ByteString
    )

    /**
     * ```tl
     * liteServer.transactionInfo#0edeed47 id:tonNode.blockIdExt proof:bytes transaction:bytes = liteServer.TransactionInfo;
     */
    @Serializable
    @TlConstructorId(0x0edeed47)
    public class TransactionInfo(
        public val id: BlockIdExt,
        public val proof: ByteString,
        public val transaction: ByteString
    )

    /**
     * ```tl
     * liteServer.transactionList#6f26c60b ids:(vector tonNode.blockIdExt) transactions:bytes = liteServer.TransactionList;
     */
    @Serializable
    @TlConstructorId(0x6f26c60b)
    public class TransactionList(
        public val ids: List<BlockIdExt>,
        public val transactions: ByteString
    )

    /**
     * ```tl
     * liteServer.transactionMetadata#ff706385 mode:# depth:int initiator:liteServer.accountId initiator_lt:long = liteServer.TransactionMetadata;
     */
    @Serializable
    @TlConstructorId(0xff706385)
    public class TransactionMetadata(
        public val mode: Int,
        public val depth: Int,
        public val initiator: AccountId,
        public val initiatorLt: Long
    )

    /**
     * ```tl
     * liteServer.transactionId#b12f65af mode:# account:mode.0?int256 lt:mode.1?long hash:mode.2?int256 metadata:mode.8?liteServer.transactionMetadata = liteServer.TransactionId;
     */
    @Serializable
    @TlConstructorId(0xb12f65af)
    public class TransactionId(
        public val mode: Int,
        @TlConditional(field = "mode", mask = 1 shl 0)
        public val account: HashBytes?,
        @TlConditional(field = "mode", mask = 1 shl 1)
        public val lt: Long?,
        @TlConditional(field = "mode", mask = 1 shl 2)
        public val hash: HashBytes?,
        @TlConditional(field = "mode", mask = 1 shl 8)
        public val metadata: TransactionMetadata?
    )

    /**
     * ```tl
     * liteServer.transactionId3#2c81da77 account:int256 lt:long = liteServer.TransactionId3;
     */
    @Serializable
    @TlConstructorId(0x2c81da77)
    public class TransactionId3(
        public val account: HashBytes,
        public val lt: Long
    )

    /**
     * ```tl
     * liteServer.blockTransactions#bd8cad2b id:tonNode.blockIdExt req_count:# incomplete:Bool ids:(vector liteServer.transactionId) proof:bytes = liteServer.BlockTransactions;
     */
    @Serializable
    @TlConstructorId(0xbd8cad2b)
    public class BlockTransactions(
        public val id: BlockIdExt,
        public val reqCount: Int,
        public val incomplete: Boolean,
        public val ids: List<TransactionId>,
        public val proof: ByteString
    )

    /**
     * ```tl
     * liteServer.blockTransactionsExt#fb8ffce4 id:tonNode.blockIdExt req_count:# incomplete:Bool transactions:bytes proof:bytes = liteServer.BlockTransactionsExt;
     */
    @Serializable
    @TlConstructorId(0xfb8ffce4)
    public class BlockTransactionsExt(
        public val id: BlockIdExt,
        public val reqCount: Int,
        public val incomplete: Boolean,
        public val transactions: ByteString,
        public val proof: ByteString
    )

    /**
     * ```tl
     * liteServer.signature#a3def855 node_id_short:int256 signature:bytes = liteServer.Signature;
     */
    @Serializable
    @TlConstructorId(0xa3def855)
    public class Signature(
        public val nodeIdShort: HashBytes,
        public val signature: ByteString
    )

    /**
     * ```tl
     * liteServer.signatureSet#f644a6e6 validator_set_hash:int catchain_seqno:int signatures:(vector liteServer.signature) = liteServer.SignatureSet;
     */
    @Serializable
    @TlConstructorId(0xf644a6e6)
    public class SignatureSet(
        public val validatorSetHash: HashBytes,
        public val catchainSeqno: Int,
        public val signatures: List<Signature>
    )

    @Serializable
    public sealed class BlockLink()

    /**
     * ```tl
     * liteServer.blockLinkBack#ef7e1bef to_key_block:Bool from:tonNode.blockIdExt to:tonNode.blockIdExt dest_proof:bytes proof:bytes state_proof:bytes = liteServer.BlockLink;
     */
    @Serializable
    @TlConstructorId(0xef7e1bef)
    public class BlockLinkBack(
        public val toKeyBlock: Boolean,
        public val from: BlockIdExt,
        public val to: BlockIdExt,
        public val destProof: ByteString,
        public val proof: ByteString,
        public val stateProof: ByteString
    ) : BlockLink()

    /**
     * ```tl
     * liteServer.blockLinkForward#520fce1c to_key_block:Bool from:tonNode.blockIdExt to:tonNode.blockIdExt dest_proof:bytes config_proof:bytes signatures:liteServer.SignatureSet = liteServer.BlockLink;
     */
    @Serializable
    @TlConstructorId(0x520fce1c)
    public class BlockLinkForward(
        public val toKeyBlock: Boolean,
        public val from: BlockIdExt,
        public val to: BlockIdExt,
        public val destProof: ByteString,
        public val configProof: ByteString,
        public val signatures: SignatureSet
    ) : BlockLink()

    /**
     * ```tl
     * liteServer.partialBlockProof#8ed0d2c1 complete:Bool from:tonNode.blockIdExt to:tonNode.blockIdExt steps:(vector liteServer.BlockLink) = liteServer.PartialBlockProof;
     */
    @Serializable
    @TlConstructorId(0x8ed0d2c1)
    public class PartialBlockProof(
        public val complete: Boolean,
        public val from: BlockIdExt,
        public val to: BlockIdExt,
        public val steps: List<BlockLink>
    )

    /**
     * ```tl
     * liteServer.configInfo#ae7b272f mode:# id:tonNode.blockIdExt state_proof:bytes config_proof:bytes = liteServer.ConfigInfo;
     */
    @Serializable
    @TlConstructorId(0xae7b272f)
    public class ConfigInfo(
        public val mode: Int,
        public val id: BlockIdExt,
        public val stateProof: ByteString,
        public val configProof: ByteString
    )

    /**
     * ```tl
     * liteServer.validatorStats#b9f796d8 mode:# id:tonNode.blockIdExt count:int complete:Bool state_proof:bytes data_proof:bytes = liteServer.ValidatorStats;
     */
    @Serializable
    @TlConstructorId(0xb9f796d8)
    public class ValidatorStats(
        public val mode: Int,
        public val id: BlockIdExt,
        public val count: Int,
        public val complete: Boolean,
        public val stateProof: ByteString,
        public val dataProof: ByteString
    )

    /**
     * ```tl
     * liteServer.libraryResult#117ab96b result:(vector liteServer.libraryEntry) = liteServer.LibraryResult;
     */
    @Serializable
    @TlConstructorId(0x117ab96b)
    public class LibraryResult(
        public val result: List<LibraryEntry>
    )

    /**
     * ```tl
     * liteServer.libraryResultWithProof#10a927bf id:tonNode.blockIdExt mode:# result:(vector liteServer.libraryEntry) state_proof:bytes data_proof:bytes = liteServer.LibraryResultWithProof;
     */
    @Serializable
    @TlConstructorId(0x10a927bf)
    public class LibraryResultWithProof(
        public val id: BlockIdExt,
        public val mode: Int,
        public val result: List<LibraryEntry>,
        public val stateProof: ByteString,
        public val dataProof: ByteString
    )

    /**
     * ```tl
     * liteServer.shardBlockLink#d30dcf72 id:tonNode.blockIdExt proof:bytes = liteServer.ShardBlockLink;
     */
    @Serializable
    @TlConstructorId(0xd30dcf72)
    public class ShardBlockLink(
        public val id: BlockIdExt,
        public val proof: ByteString
    )

    /**
     * ```tl
     * liteServer.shardBlockProof#1d62a07a masterchain_id:tonNode.blockIdExt links:(vector liteServer.shardBlockLink) = liteServer.ShardBlockProof;
     */
    @Serializable
    @TlConstructorId(0x1d62a07a)
    public class ShardBlockProof(
        public val masterchainId: BlockIdExt,
        public val links: List<ShardBlockLink>
    )

    /**
     * ```tl
     * liteServer.lookupBlockResult#99786be7 id:tonNode.blockIdExt mode:# mc_block_id:tonNode.blockIdExt client_mc_state_proof:bytes mc_block_proof:bytes shard_links:(vector liteServer.shardBlockLink) header:bytes prev_header:bytes = liteServer.LookupBlockResult;
     */
    @Serializable
    @TlConstructorId(0x99786be7)
    public class LookupBlockResult(
        public val id: BlockIdExt,
        public val mode: Int,
        public val mcBlockId: BlockIdExt,
        public val clientMcStateProof: ByteString,
        public val mcBlockProof: ByteString,
        public val shardLinks: List<ShardBlockLink>,
        public val header: ByteString,
        public val prevHeader: ByteString
    )

    /**
     * ```tl
     * liteServer.outMsgQueueSize#a7c64c85 id:tonNode.blockIdExt size:int = liteServer.OutMsgQueueSize;
     */
    @Serializable
    @TlConstructorId(0xa7c64c85)
    public class OutMsgQueueSize(
        public val id: BlockIdExt,
        public val size: Int
    )

    /**
     * ```tl
     * liteServer.outMsgQueueSizes#f8504a03 shards:(vector liteServer.outMsgQueueSize) ext_msg_queue_size_limit:int = liteServer.OutMsgQueueSizes;
     */
    @Serializable
    @TlConstructorId(0xf8504a03)
    public class OutMsgQueueSizes(
        public val shards: List<OutMsgQueueSize>,
        public val extMsgQueueSizeLimit: Int
    )

    /**
     * ```tl
     * liteServer.blockOutMsgQueueSize#8acdbe1b mode:# id:tonNode.blockIdExt size:long proof:mode.0?bytes = liteServer.BlockOutMsgQueueSize;
     */
    @Serializable
    @TlConstructorId(0x8acdbe1b)
    public class BlockOutMsgQueueSize(
        public val mode: Int,
        public val id: BlockIdExt,
        public val size: Long,
        @TlConditional(field = "mode", mask = 1 shl 0)
        public val proof: ByteString?
    )

    /**
     * ```tl
     * liteServer.accountDispatchQueueInfo#9b52aabb addr:int256 size:long min_lt:long max_lt:long = liteServer.AccountDispatchQueueInfo;
     */
    @Serializable
    @TlConstructorId(0x9b52aabb)
    public class AccountDispatchQueueInfo(
        public val addr: HashBytes,
        public val size: Long,
        public val minLt: Long,
        public val maxLt: Long
    )

    /**
     * ```tl
     * liteServer.dispatchQueueInfo#5d1132d0 mode:# id:tonNode.blockIdExt account_dispatch_queues:(vector liteServer.accountDispatchQueueInfo) complete:Bool proof:mode.0?bytes = liteServer.DispatchQueueInfo;
     */
    @Serializable
    @TlConstructorId(0x5d1132d0)
    public class DispatchQueueInfo(
        public val mode: Int,
        public val id: BlockIdExt,
        public val accountDispatchQueues: List<AccountDispatchQueueInfo>,
        public val complete: Boolean,
        @TlConditional(field = "mode", mask = 1 shl 0)
        public val proof: ByteString?
    )

    /**
     * ```tl
     * liteServer.dispatchQueueMessage#84c423ea addr:int256 lt:long hash:int256 metadata:liteServer.transactionMetadata = liteServer.DispatchQueueMessage;
     */
    @Serializable
    @TlConstructorId(0x84c423ea)
    public class DispatchQueueMessage(
        public val addr: HashBytes,
        public val lt: Long,
        public val hash: HashBytes,
        public val metadata: TransactionMetadata
    )

    /**
     * ```tl
     * liteServer.dispatchQueueMessages#4b407931
     *     mode:#
     *     id:tonNode.blockIdExt
     *     messages:(vector liteServer.dispatchQueueMessage)
     *     complete:Bool
     *     proof:mode.0?bytes
     *     messages_boc:mode.2?bytes
     *     = liteServer.DispatchQueueMessages;
     */
    @Serializable
    @TlConstructorId(0x4b407931)
    public class DispatchQueueMessages(
        public val mode: Int,
        public val id: BlockIdExt,
        public val messages: List<DispatchQueueMessage>,
        public val complete: Boolean,
        @TlConditional(field = "mode", mask = 1 shl 0)
        public val proof: ByteString?,
        @TlConditional(field = "mode", mask = 1 shl 2)
        public val messagesBoc: ByteString?
    )

    /**
     * ```tl
     * liteServer.debug.verbosity#5d404733 value:int = liteServer.debug.Verbosity;
     */
    @Serializable
    @TlConstructorId(0x5d404733)
    public class DebugVerbosity(
        public val value: Int
    )

    /**
     * ```tl
     * liteServer.nonfinal.candidateId#55047fee block_id:tonNode.blockIdExt creator:int256 collated_data_hash:int256 = liteServer.nonfinal.CandidateId;
     */
    @Serializable
    @TlConstructorId(0x55047fee)
    public class NonfinalCandidateId(
        public val blockId: BlockIdExt,
        public val creator: HashBytes,
        public val collatedDataHash: HashBytes
    )

    /**
     * ```tl
     * liteServer.nonfinal.candidate#80c3468c id:liteServer.nonfinal.candidateId data:bytes collated_data:bytes = liteServer.nonfinal.Candidate;
     */
    @Serializable
    @TlConstructorId(0x80c3468c)
    public class NonfinalCandidate(
        public val id: NonfinalCandidateId,
        public val data: ByteString,
        public val collatedData: ByteString
    )

    /**
     * ```tl
     * liteServer.nonfinal.candidateInfo#4dec01d5 id:liteServer.nonfinal.candidateId available:Bool approved_weight:long signed_weight:long total_weight:long = liteServer.nonfinal.CandidateInfo;
     */
    @Serializable
    @TlConstructorId(0x4dec01d5)
    public class NonfinalCandidateInfo(
        public val id: NonfinalCandidateId,
        public val available: Boolean,
        public val approvedWeight: Long,
        public val signedWeight: Long,
        public val totalWeight: Long
    )

    /**
     * ```tl
     * liteServer.nonfinal.validatorGroupInfo#f9d68aa7 next_block_id:tonNode.blockId cc_seqno:int prev:(vector tonNode.blockIdExt) candidates:(vector liteServer.nonfinal.candidateInfo) = liteServer.nonfinal.ValidatorGroupInfo;
     */
    @Serializable
    @TlConstructorId(0xf9d68aa7)
    public class NonfinalValidatorGroupInfo(
        public val nextBlockId: BlockIdExt,
        public val ccSeqno: Int,
        public val prev: List<BlockIdExt>,
        public val candidates: List<NonfinalCandidateInfo>
    )

    /**
     * ```tl
     * liteServer.nonfinal.validatorGroups#8d0b9dfe groups:(vector liteServer.nonfinal.validatorGroupInfo) = liteServer.nonfinal.ValidatorGroups;
     */
    @Serializable
    @TlConstructorId(0x8d0b9dfe)
    public class NonfinalValidatorGroups(
        public val groups: List<NonfinalValidatorGroupInfo>
    )

    public interface Function : LiteApi

    /**
     * ```tl
     * liteServer.getMasterchainInfo#89b5e62e = liteserver.GetMasterchainInfo;
     */
    @Serializable
    @TlConstructorId(0x89b5e62e)
    public object GetMasterchainInfo : Function

    /**
     * ```tl
     * liteServer.getMasterchainInfoExt#70a671df mode:# = liteServer.MasterchainInfoExt;
     */
    @Serializable
    @TlConstructorId(0x70a671df)
    public class GetMasterchainInfoExt(
        public val mode: Int
    )

    /**
     * ```tl
     * liteServer.getTime#16ad5a34 = liteServer.CurrentTime;
     */
    @Serializable
    @TlConstructorId(0x16ad5a34)
    public object GetTime : Function

    /**
     * ```tl
     * liteServer.getVersion#232b940b = liteServer.Version;
     */
    @Serializable
    @TlConstructorId(0x232b940b)
    public object GetVersion : Function

    /**
     * ```tl
     * liteServer.getState#ba6e2eb6 id:tonNode.blockIdExt = liteServer.BlockState;
     */
    @Serializable
    @TlConstructorId(0xba6e2eb6)
    public class GetState(
        public val id: BlockIdExt
    ) : Function

    @Serializable
    @TlConstructorId(0x85832881)
    public class SendMessage(
        public val body: ByteString
    ) : Function


    @Serializable
    @TlConstructorId(0x798c06df)
    public class LiteServerQuery(
        public val data: ByteString
    ) : Function
}
