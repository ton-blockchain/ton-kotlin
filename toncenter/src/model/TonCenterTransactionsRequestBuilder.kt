package org.ton.sdk.toncenter.model

import org.ton.kotlin.crypto.HashBytes
import org.ton.sdk.blockchain.address.AddressStd

public class TonCenterTransactionsRequestBuilder {
    public var workchain: Int? = null

    public var shard: Long? = null

    public var seqno: Int? = null

    public var account: MutableList<AddressStd> = ArrayList()

    public var excludeAccount: MutableList<AddressStd> = ArrayList()

    public var hash: HashBytes? = null

    public var lt: Long? = null

    public var startUTime: Long? = null

    public var endUTime: Long? = null

    public var startLt: Long? = null

    public var endLt: Long? = null

    public var limit: Int? = null

    public var offset: Int? = null

    public fun workchain(workchain: Int): TonCenterTransactionsRequestBuilder = apply {
        this.workchain = workchain
    }

    public fun shard(shard: Long): TonCenterTransactionsRequestBuilder = apply {
        this.shard = shard
    }

    public fun seqno(seqno: Int): TonCenterTransactionsRequestBuilder = apply {
        this.seqno = seqno
    }

    public fun address(vararg address: AddressStd): TonCenterTransactionsRequestBuilder = apply {
        this.account.addAll(address)
    }

    public fun address(address: Collection<AddressStd>): TonCenterTransactionsRequestBuilder = apply {
        this.account.addAll(address)
    }

    public fun excludeAddress(vararg address: AddressStd): TonCenterTransactionsRequestBuilder = apply {
        this.excludeAccount.addAll(address)
    }

    public fun excludeAddress(address: Collection<AddressStd>): TonCenterTransactionsRequestBuilder = apply {
        this.excludeAccount.addAll(address)
    }

    public fun hash(hash: HashBytes): TonCenterTransactionsRequestBuilder = apply {
        this.hash = hash
    }

    public fun lt(lt: Long): TonCenterTransactionsRequestBuilder = apply {
        this.lt = lt
    }

    public fun startUTime(startUTime: Long): TonCenterTransactionsRequestBuilder = apply {
        this.startUTime = startUTime
    }

    public fun endUTime(endUTime: Long): TonCenterTransactionsRequestBuilder = apply {
        this.endUTime = endUTime
    }

    public fun startLt(startLt: Long): TonCenterTransactionsRequestBuilder = apply {
        this.startLt = startLt
    }

    public fun endLt(endLt: Long): TonCenterTransactionsRequestBuilder = apply {
        this.endLt = endLt
    }

    public fun limit(limit: Int): TonCenterTransactionsRequestBuilder = apply {
        this.limit = limit
    }

    public fun offset(offset: Int): TonCenterTransactionsRequestBuilder = apply {
        this.offset = offset
    }
}
