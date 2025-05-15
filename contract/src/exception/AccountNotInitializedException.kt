package org.ton.kotlin.contract.exception

import org.ton.kotlin.block.MsgAddressInt

public class AccountNotInitializedException(
    public val address: MsgAddressInt
) : RuntimeException("Account not initialized: $address")
