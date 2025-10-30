package org.ton.sdk.toncenter.model

public interface TonCenterRequestBuilder {
    public var timeout: Long?

    public var noAddressBook: Boolean?

    public var noMetadata: Boolean?
}

public interface TonCenterLimitRequestBuilder {
    public var limit: Int?

    public var offset: Int?
}
