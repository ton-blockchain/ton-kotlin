package org.ton.lite.client.internal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.bitstring.BitString
import org.ton.bitstring.toBitString
import org.ton.kotlin.account.Account
import org.ton.kotlin.transaction.Transaction
import org.ton.tlb.CellRef
import kotlin.jvm.JvmName

internal data class BlockHeaderResult(
    val time: Int,
    val lt: Long,
    val stateHash: BitString?
)

public data class FullAccountState(
    @SerialName("block_id")
    @get:JvmName("blockId")
    public val blockId: TonNodeBlockIdExt,

    @get:JvmName("address")
    public val address: org.ton.kotlin.message.address.MsgAddressInt,

    @SerialName("last_transaction_id")
    @get:JvmName("lastTransactionId")
    public val lastTransactionId: TransactionId?,

    @get:JvmName("account")
    public val account: CellRef<Account?>
)

@Serializable
public data class TransactionId(
    @get:JvmName("hash")
    val hash: BitString,

    @get:JvmName("lt")
    val lt: Long
) {
    public constructor(hash: ByteArray, lt: Long) : this(hash.toBitString(), lt)

    init {
        require(hash.size == 256) { "expected hash.size == 256, actual: ${hash.size}" }
    }
}

public data class TransactionInfo(
    @SerialName("block_id")
    @get:JvmName("blockId")
    val blockId: TonNodeBlockIdExt,

    @get:JvmName("id")
    val id: TransactionId,

    @get:JvmName("transaction")
    val transaction: CellRef<Transaction>
)
