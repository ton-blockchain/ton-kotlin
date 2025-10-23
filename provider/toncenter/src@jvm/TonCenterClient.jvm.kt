package org.ton.kotlin.provider.toncenter

import io.ktor.client.*
import kotlinx.coroutines.DelicateCoroutinesApi
import org.ton.kotlin.provider.toncenter.internal.TonCenterV3ClientImpl
import org.ton.kotlin.provider.toncenter.model.*

@OptIn(DelicateCoroutinesApi::class)
public actual interface TonCenterV3Client {
    /**
     * Fetches the account states for the specified addresses from the TON blockchain.
     *
     * @param request The request object containing a list of addresses and a flag to include serialized
     *                account states (BoC) in the response.
     * @return [TonCenterAccountStatesResponse] containing the states of the specified accounts,
     *         related address book entries, and metadata.
     */
    public actual suspend fun accountStates(request: TonCenterAccountRequest): TonCenterAccountStatesResponse

    /**
     * Sends a message to the TON blockchain using the provided request data.
     *
     * @param request The request object containing the serialized message body (BOC) to be sent.
     * @return [TonCenterSendMessageResult] of the message send request, including the hash of the message
     * and its normalized hash.
     */
    public actual suspend fun message(request: TonCenterSendMessageRequest): TonCenterSendMessageResult

    /**
     * Executes a get method call on a specific smart contract in the TON blockchain.
     *
     * @param request The request object containing the address of the smart contract, the method name to be called,
     * and the stack arguments required for the method invocation.
     * @return [TonCenterRunGetMethodResult] containing the gas used, the exit code of the method execution,
     * and the resulting stack values.
     */
    public actual suspend fun runGetMethod(request: TonCenterRunGetMethodRequest): TonCenterRunGetMethodResult

    public actual companion object {
        @JvmStatic
        public actual fun create(): TonCenterV3Client {
            return create(HttpClient())
        }

        @JvmStatic
        public actual fun create(endpoint: String): TonCenterV3Client {
            return create(HttpClient(), endpoint)
        }

        @JvmStatic
        public actual fun create(httpClient: HttpClient): TonCenterV3Client {
            return TonCenterV3ClientImpl(httpClient, "https://toncenter.com")
        }

        @JvmStatic
        public actual fun create(httpClient: HttpClient, endpoint: String): TonCenterV3Client {
            return TonCenterV3ClientImpl(httpClient, endpoint)
        }
    }

    public actual suspend fun addressBook(request: TonCenterAddressBookRequest): TonCenterAddressBook

    public actual suspend fun metadata(request: TonCenterMetadataRequest): TonCenterMetadata

    public actual suspend fun walletStates(request: TonCenterAccountRequest): TonCenterWalletStatesResponse

    public actual suspend fun masterchainInfo(): TonCenterMasterchainInfo

    public actual suspend fun masterchainBlockShards(request: TonCenterBlocksRequestBuilder): TonCenterBlocksResponse

    public actual suspend fun blocks(request: TonCenterBlocksRequestBuilder): TonCenterBlocksResponse

    public actual suspend fun masterchainBlockShardState(request: TonCenterBlocksRequestBuilder): TonCenterBlocksResponse
}
