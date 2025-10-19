package org.ton.kotlin.provider.toncenter

import io.ktor.client.*
import org.ton.kotlin.provider.toncenter.internal.TonCenterV3ClientImpl
import org.ton.kotlin.provider.toncenter.model.*

public actual interface TonCenterV3Client {
    public actual suspend fun masterchainInfo(): TonCenterMasterchainInfo

    public actual companion object {
        public actual fun create(httpClient: HttpClient): TonCenterV3Client {
            return TonCenterV3ClientImpl(httpClient, "https://toncenter.com")
        }

        public actual fun create(httpClient: HttpClient, endpoint: String): TonCenterV3Client {
            return TonCenterV3ClientImpl(httpClient, endpoint)
        }
    }

    public actual suspend fun message(request: TonCenterSendMessageRequest): TonCenterSendMessageResult
    public actual suspend fun runGetMethod(request: TonCenterRunGetMethodRequest): TonCenterRunGetMethodResult
    public actual suspend fun accountStates(request: TonCenterAccountStatesRequest): TonCenterAccountStatesResponse
}
