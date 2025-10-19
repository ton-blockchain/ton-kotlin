@file:Suppress("NOTHING_TO_INLINE")

package org.ton.kotlin.provider.toncenter

import io.ktor.client.*
import org.ton.kotlin.provider.toncenter.model.*

/**
 * Creates a new instance of [TonCenterV3Client] using the provided HTTP client and endpoint.
 *
 * @param httpClient The HTTP client used for making network requests.
 * @param endpoint The endpoint URL for the TON Center API.
 * @return A new instance of [TonCenterV3Client].
 */
public inline fun TonCenterV3Client(
    httpClient: HttpClient,
    endpoint: String = "https://toncenter.com"
): TonCenterV3Client = TonCenterV3Client.create(httpClient, endpoint)

public expect interface TonCenterV3Client {
    /**
     * Fetches the account states for the specified addresses from the TON blockchain.
     *
     * @param request The request object containing a list of addresses and a flag to include serialized
     *                account states (BoC) in the response.
     * @return [TonCenterAccountStatesResponse] containing the states of the specified accounts,
     *         related address book entries, and metadata.
     */
    public suspend fun accountStates(request: TonCenterAccountStatesRequest): TonCenterAccountStatesResponse

    /**
     * Sends a message to the TON blockchain using the provided request data.
     *
     * @param request The request object containing the serialized message body (BOC) to be sent.
     * @return [TonCenterSendMessageResult] of the message send request, including the hash of the message
     * and its normalized hash.
     */
    public suspend fun message(request: TonCenterSendMessageRequest): TonCenterSendMessageResult

    /**
     * Executes a get method call on a specific smart contract in the TON blockchain.
     *
     * @param request The request object containing the address of the smart contract, the method name to be called,
     * and the stack arguments required for the method invocation.
     * @return [TonCenterRunGetMethodResult] containing the gas used, the exit code of the method execution,
     * and the resulting stack values.
     */
    public suspend fun runGetMethod(request: TonCenterRunGetMethodRequest): TonCenterRunGetMethodResult

    /**
     * Retrieves the first and last indexed blocks in the masterchain.
     *
     * @return [TonCenterMasterchainInfo] containing the first and last
     * indexed blocks in the masterchain.
     */
    public suspend fun masterchainInfo(): TonCenterMasterchainInfo

    public companion object {
        /**
         * Creates a new instance of [TonCenterV3Client] using default settings.
         *
         * @return A new instance of [TonCenterV3Client].
         */
        public fun create(): TonCenterV3Client

        /**
         * Creates a new instance of [TonCenterV3Client] using the specified endpoint.
         *
         * @param endpoint The endpoint URL for the TON Center API.
         * @return A new instance of [TonCenterV3Client].
         */
        public fun create(endpoint: String): TonCenterV3Client

        /**
         * Creates a new instance of [TonCenterV3Client] using the specified HTTP client.
         *
         * @param httpClient The HTTP client used for making network requests.
         * @return A new instance of [TonCenterV3Client].
         */
        public fun create(httpClient: HttpClient): TonCenterV3Client

        /**
         * Creates a new instance of [TonCenterV3Client] using the provided HTTP client and endpoint.
         *
         * @param httpClient The HTTP client used for making network requests.
         * @param endpoint The endpoint URL for the TON Center API.
         * @return A new instance of [TonCenterV3Client].
         */
        public fun create(httpClient: HttpClient, endpoint: String): TonCenterV3Client
    }
}
