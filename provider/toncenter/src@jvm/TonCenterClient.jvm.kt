package org.ton.kotlin.provider.toncenter

import io.ktor.client.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import org.ton.kotlin.provider.toncenter.internal.TonCenterV3ClientImpl
import org.ton.kotlin.provider.toncenter.model.*
import java.util.concurrent.CompletableFuture

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
    public actual suspend fun accountStates(request: TonCenterAccountStatesRequest): TonCenterAccountStatesResponse

    /**
     * Asynchronously fetches the account states for the specified addresses from the TON blockchain.
     *
     * @param request The request object containing a list of addresses and a flag indicating whether
     *                to include serialized account states (BoC) in the response.
     * @return A [CompletableFuture] containing [TonCenterAccountStatesResponse], which includes the
     *         states of the specified accounts, related address book entries, and metadata.
     */
    public fun accountStatesAsync(request: TonCenterAccountStatesRequest): CompletableFuture<TonCenterAccountStatesResponse> =
        GlobalScope.future { accountStates(request) }

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

    /**
     * Executes a get method call on a specific smart contract in the TON blockchain.
     *
     * @param request The request object containing the address of the smart contract, the method name to be called,
     * and the stack arguments required for the method invocation.
     * @return A [CompletableFuture] containing [TonCenterRunGetMethodResult] with the gas used, the exit code of the method execution,
     * and the resulting stack values.
     */
    public fun runGetMethodAsync(request: TonCenterRunGetMethodRequest): CompletableFuture<TonCenterRunGetMethodResult> =
        GlobalScope.future { runGetMethod(request) }

    /**
     * Sends a message to the TON blockchain asynchronously using the provided request data.
     *
     * @param request The request object containing the serialized message body (BOC) to be sent.
     * @return A [CompletableFuture] containing the result of the message send operation,
     * which includes the hash of the message and its normalized hash.
     */
    public fun messageAsync(request: TonCenterSendMessageRequest): CompletableFuture<TonCenterSendMessageResult> =
        GlobalScope.future { message(request) }

    /**
     * Retrieves the first and last indexed blocks in the masterchain.
     *
     * @return [TonCenterMasterchainInfo] containing the first and last
     * indexed blocks in the masterchain.
     */
    public actual suspend fun masterchainInfo(): TonCenterMasterchainInfo

    /**
     * Asynchronously retrieves the first and last indexed blocks in the masterchain.
     *
     * @return A [CompletableFuture] of [TonCenterMasterchainInfo] containing the first and last
     * indexed blocks in the masterchain.
     */
    public fun masterchainInfoAsync(): CompletableFuture<TonCenterMasterchainInfo> =
        GlobalScope.future { masterchainInfo() }

    public actual companion object {
        @JvmStatic
        public actual fun create(httpClient: HttpClient): TonCenterV3Client {
            return TonCenterV3ClientImpl(httpClient, "https://toncenter.com")
        }

        @JvmStatic
        public actual fun create(httpClient: HttpClient, endpoint: String): TonCenterV3Client {
            return TonCenterV3ClientImpl(httpClient, endpoint)
        }
    }
}
