package org.ton.kotlin.provider.toncenter.model

public class TonCenterBlocksRequestBuilder(
    public var workchain: Int? = null,
    public var shard: Long? = null,
    public var seqno: Int? = null,
    public var mcSeqno: Int? = null,
    public var startUTime: Long? = null,
    public var endUTime: Long? = null,
    public var startLt: Long? = null,
    public var endLt: Long? = null,
    public var limit: Int? = null,
    public var offset: Int? = null,
)
