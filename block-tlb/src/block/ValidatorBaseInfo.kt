package org.ton.kotlin.block

import kotlinx.serialization.SerialName


@SerialName("validator_base_info")
public data class ValidatorBaseInfo(
    val validator_list_hash_short: Long,
    val catchain_seqno: Long
)
