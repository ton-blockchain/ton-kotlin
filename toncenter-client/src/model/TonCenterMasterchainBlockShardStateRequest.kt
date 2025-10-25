package org.ton.sdk.toncenter.model

public class TonCenterMasterchainBlockShardStateRequest(
    public val seqno: Int,
    public var limit: Int?,
    public var offset: Int?
) {
    public class Builder(
        public var seqno: Int
    ) {
        public var limit: Int? = null
        public var offset: Int? = null

        public fun build(): TonCenterMasterchainBlockShardStateRequest = TonCenterMasterchainBlockShardStateRequest(
            seqno,
            limit,
            offset
        )
    }
}
