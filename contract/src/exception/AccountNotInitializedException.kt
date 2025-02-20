package org.ton.contract.exception

public class AccountNotInitializedException(
    public val address: org.ton.kotlin.message.address.MsgAddressInt
) : RuntimeException("Account not initialized: $address")
