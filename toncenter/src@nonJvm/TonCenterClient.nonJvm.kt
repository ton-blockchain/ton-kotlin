package org.ton.sdk.toncenter

import io.ktor.client.*
import org.ton.sdk.toncenter.internal.TonCenterV3ClientImpl
import org.ton.sdk.toncenter.model.*

public actual interface TonCenterV3Client {
    public actual suspend fun accountStates(request: TonCenterAccountRequest): TonCenterAccountStatesResponse
    public actual suspend fun runGetMethod(request: TonCenterRunGetMethodRequest): TonCenterRunGetMethodResult
    public actual suspend fun message(request: TonCenterSendMessageRequest): TonCenterSendMessageResult
    public actual suspend fun masterchainInfo(): TonCenterMasterchainInfo

    public actual companion object {
        public actual fun create(): TonCenterV3Client {
            return create(HttpClient())
        }

        public actual fun create(endpoint: String): TonCenterV3Client {
            return create(HttpClient(), endpoint)
        }

        public actual fun create(httpClient: HttpClient): TonCenterV3Client {
            return TonCenterV3ClientImpl(httpClient, "https://toncenter.com")
        }

        public actual fun create(httpClient: HttpClient, endpoint: String): TonCenterV3Client {
            return TonCenterV3ClientImpl(httpClient, endpoint)
        }
    }

    public actual suspend fun addressBook(request: TonCenterAddressBookRequest): TonCenterAddressBook
    public actual suspend fun metadata(request: TonCenterMetadataRequest): TonCenterMetadata
    public actual suspend fun walletStates(request: TonCenterAccountRequest): TonCenterWalletStatesResponse
    public actual suspend fun masterchainBlockShards(request: TonCenterBlocksRequestBuilder): TonCenterBlocksResponse
    public actual suspend fun blocks(request: TonCenterBlocksRequestBuilder): TonCenterBlocksResponse
    public actual suspend fun masterchainBlockShardState(request: TonCenterBlocksRequestBuilder): TonCenterBlocksResponse
    public actual suspend fun transactions(request: TonCenterTransactionsRequestBuilder): TonCenterTransactionsResponse
}
