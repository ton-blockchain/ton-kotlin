package org.ton.sdk.toncenter.internal

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.ton.sdk.toncenter.client.TonCenterV3Client
import org.ton.sdk.toncenter.model.*
import kotlin.io.encoding.Base64

internal class TonCenterV3ClientImpl(
    val httpClient: HttpClient,
    val endpoint: String
) : TonCenterV3Client {
    private val json = Json {
        @Suppress("OPT_IN_USAGE")
        namingStrategy = JsonNamingStrategy.SnakeCase
        @Suppress("OPT_IN_USAGE")
        decodeEnumsCaseInsensitive = true
    }

    override suspend fun accountStates(request: TonCenterAccountRequest): TonCenterAccountStatesResponse {
        return json.decodeFromString<TonCenterAccountStatesResponse>(
            httpClient.get("$endpoint/api/v3/accountStates") {
                request.address.forEach {
                    parameter("address", it.toBase64String(false))
                }
                parameter("include_boc", request.includeBoc)
            }.bodyAsText()
        )
    }

    override suspend fun addressBook(request: TonCenterAddressBookRequest): TonCenterAddressBook {
        return json.decodeFromString<TonCenterAddressBook>(
            httpClient.get("$endpoint/api/v3/addressBook") {
                request.address.forEach {
                    parameter("address", it.toBase64String(false))
                }
            }.bodyAsText()
        )
    }

    override suspend fun metadata(request: TonCenterMetadataRequest): TonCenterMetadata {
        return json.decodeFromString<TonCenterMetadata>(
            httpClient.get("$endpoint/api/v3/metadata") {
                request.address.forEach {
                    parameter("address", it.toBase64String(false))
                }
            }.bodyAsText()
        )
    }

    override suspend fun walletStates(request: TonCenterAccountRequest): TonCenterWalletStatesResponse {
        return json.decodeFromString<TonCenterWalletStatesResponse>(
            httpClient.get("$endpoint/api/v3/walletStates") {
                request.address.forEach {
                    parameter("address", it.toBase64String(false))
                }
            }.bodyAsText()
        )
    }

    override suspend fun message(request: TonCenterSendMessageRequest): TonCenterSendMessageResult {
        return json.decodeFromString<TonCenterSendMessageResult>(
            httpClient.post("$endpoint/api/v3/message") {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString<TonCenterSendMessageRequest>(request))
            }.bodyAsText()
        )
    }

    override suspend fun runGetMethod(request: TonCenterRunGetMethodRequest): TonCenterRunGetMethodResult {
        return json.decodeFromString<TonCenterRunGetMethodResult>(
            httpClient.post("$endpoint/api/v3/runGetMethod") {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString<TonCenterRunGetMethodRequest>(request))
            }.bodyAsText()
        )
    }

    override suspend fun masterchainInfo(): TonCenterMasterchainInfo {
        return json.decodeFromString<TonCenterMasterchainInfo>(
            httpClient.get("$endpoint/api/v3/masterchainInfo").bodyAsText()
        )
    }

    override suspend fun masterchainBlockShardState(request: TonCenterBlocksRequestBuilder): TonCenterBlocksResponse {
        return json.decodeFromString<TonCenterBlocksResponse>(
            httpClient.get("$endpoint/api/v3/masterchainBlockShardState") {
                parameter("seqno", request.seqno)
                request.limit?.let { parameter("limit", it) }
                request.offset?.let { parameter("offset", it) }
            }.bodyAsText()
        )
    }

    override suspend fun masterchainBlockShards(request: TonCenterBlocksRequestBuilder): TonCenterBlocksResponse {
        return json.decodeFromString<TonCenterBlocksResponse>(
            httpClient.get("$endpoint/api/v3/masterchainBlockShardState") {
                request.seqno?.let { parameter("seqno", it) }
                request.limit?.let { parameter("limit", it) }
                request.offset?.let { parameter("offset", it) }
            }.bodyAsText()
        )
    }

    override suspend fun blocks(request: TonCenterBlocksRequestBuilder): TonCenterBlocksResponse {
        return json.decodeFromString<TonCenterBlocksResponse>(
            httpClient.get("$endpoint/api/v3/blocks") {
                request.workchain?.let { parameter("workchain", it) }
                request.shard?.let { parameter("shard", it) }
                request.seqno?.let { parameter("seqno", it) }
                request.mcSeqno?.let { parameter("mc_seqno", it) }
                request.startUTime?.let { parameter("start_utime", it) }
                request.endUTime?.let { parameter("end_utime", it) }
                request.startLt?.let { parameter("start_lt", it) }
                request.endLt?.let { parameter("end_lt", it) }
                request.limit?.let { parameter("limit", it) }
                request.offset?.let { parameter("offset", it) }
            }.bodyAsText()
        )
    }

    override suspend fun transactions(request: TonCenterTransactionsRequestBuilder): TonCenterTransactionsResponse {
        val response = httpClient.get("$endpoint/api/v3/transactions") {
            request.workchain?.let { parameter("workchain", it) }
            request.shard?.let { parameter("shard", it) }
            request.seqno?.let { parameter("seqno", it) }
            request.account.forEach {
                parameter("account", it.toBase64String(Base64.UrlSafe, false))
            }
            request.excludeAccount.forEach {
                parameter("exclude_account", it.toBase64String(Base64.UrlSafe, false))
            }
            request.hash?.let { parameter("hash", it) }
            request.lt?.let { parameter("lt", it) }
            request.startUTime?.let { parameter("start_utime", it) }
            request.endUTime?.let { parameter("end_utime", it) }
            request.startLt?.let { parameter("start_lt", it) }
            request.endLt?.let { parameter("end_lt", it) }
            request.limit?.let { parameter("limit", it) }
            request.offset?.let { parameter("offset", it) }
        }.bodyAsText()
        return runCatching {
            json.decodeFromString<TonCenterTransactionsResponse>(
                response
            )
        }.onFailure {
            throw IllegalStateException("Failed to parse response: $response", it)
        }.getOrThrow()
    }
}
