package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.bitstring.BitString
import org.ton.kotlin.message.address.MsgAddressInt


@SerialName("smc_info")
public data class SmartContractInfo(
    val actions: Int,
    val msgs_sent: Int,
    val unixtime: Long,
    val block_lt: Long,
    val trans_lt: Long,
    val rand_seed: BitString,
    val balance_remaining: CurrencyCollection,
    val myself: MsgAddressInt
) {
    init {
        require(rand_seed.size == 256) { "required: rand_seed.size == 256, actual: ${rand_seed.size}" }
    }
}
