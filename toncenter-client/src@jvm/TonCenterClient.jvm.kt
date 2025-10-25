package org.ton.sdk.toncenter.client

import io.ktor.client.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import org.ton.sdk.toncenter.internal.TonCenterV3ClientImpl
import org.ton.sdk.toncenter.model.*
import java.util.concurrent.Future

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
    @JvmSynthetic
    public actual suspend fun accountStates(request: TonCenterAccountRequest): TonCenterAccountStatesResponse

    public fun accountStatesAsync(request: TonCenterAccountRequest): Future<TonCenterAccountStatesResponse> =
        GlobalScope.future { accountStates(request) }

    /**
     * Sends a message to the TON blockchain using the provided request data.
     *
     * @param request The request object containing the serialized message body (BOC) to be sent.
     * @return [TonCenterSendMessageResult] of the message send request, including the hash of the message
     * and its normalized hash.
     */
    @JvmSynthetic
    public actual suspend fun message(request: TonCenterSendMessageRequest): TonCenterSendMessageResult

    public fun messageAsync(request: TonCenterSendMessageRequest): Future<TonCenterSendMessageResult> =
        GlobalScope.future { message(request) }

    /**
     * Executes a get method call on a specific smart contract in the TON blockchain.
     *
     * @param request The request object containing the address of the smart contract, the method name to be called,
     * and the stack arguments required for the method invocation.
     * @return [TonCenterRunGetMethodResult] containing the gas used, the exit code of the method execution,
     * and the resulting stack values.
     */
    @JvmSynthetic
    public actual suspend fun runGetMethod(request: TonCenterRunGetMethodRequest): TonCenterRunGetMethodResult

    public fun runGetMethodAsync(request: TonCenterRunGetMethodRequest): Future<TonCenterRunGetMethodResult> =
        GlobalScope.future { runGetMethod(request) }

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

    @JvmSynthetic
    public actual suspend fun addressBook(request: TonCenterAddressBookRequest): TonCenterAddressBook

    public fun addressBookAsync(request: TonCenterAddressBookRequest): Future<TonCenterAddressBook> =
        GlobalScope.future { addressBook(request) }

    @JvmSynthetic
    public actual suspend fun metadata(request: TonCenterMetadataRequest): TonCenterMetadata

    public fun metadataAsync(request: TonCenterMetadataRequest): Future<TonCenterMetadata> =
        GlobalScope.future { metadata(request) }

    @JvmSynthetic
    public actual suspend fun walletStates(request: TonCenterAccountRequest): TonCenterWalletStatesResponse

    public fun walletStatesAsync(request: TonCenterAccountRequest): Future<TonCenterWalletStatesResponse> =
        GlobalScope.future { walletStates(request) }

    @JvmSynthetic
    public actual suspend fun masterchainInfo(): TonCenterMasterchainInfo

    public fun masterchainInfoAsync(): Future<TonCenterMasterchainInfo> =
        GlobalScope.future { masterchainInfo() }

    @JvmSynthetic
    public actual suspend fun masterchainBlockShards(request: TonCenterBlocksRequestBuilder): TonCenterBlocksResponse

    public fun masterchainBlockShardsAsync(request: TonCenterBlocksRequestBuilder): Future<TonCenterBlocksResponse> =
        GlobalScope.future { masterchainBlockShards(request) }

    @JvmSynthetic
    public actual suspend fun blocks(request: TonCenterBlocksRequestBuilder): TonCenterBlocksResponse

    public fun blocksAsync(request: TonCenterBlocksRequestBuilder): Future<TonCenterBlocksResponse> =
        GlobalScope.future { blocks(request) }

    @JvmSynthetic
    public actual suspend fun masterchainBlockShardState(request: TonCenterBlocksRequestBuilder): TonCenterBlocksResponse

    public fun masterchainBlockShardStateAsync(request: TonCenterBlocksRequestBuilder): Future<TonCenterBlocksResponse> =
        GlobalScope.future { masterchainBlockShardState(request) }

    @JvmSynthetic
    public actual suspend fun transactions(request: TonCenterTransactionsRequestBuilder): TonCenterTransactionsResponse

    public fun transactionsAsync(request: TonCenterTransactionsRequestBuilder): Future<TonCenterTransactionsResponse> =
        GlobalScope.future { transactions(request) }
}
