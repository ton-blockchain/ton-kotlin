package org.ton.kotlin.provider.toncenter.internal

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.ton.kotlin.provider.toncenter.TonCenterV3Client
import org.ton.kotlin.provider.toncenter.model.*

internal class TonCenterV3ClientImpl(
    val httpClient: HttpClient,
    val endpoint: String
) : TonCenterV3Client {
    private val json = Json {
        @Suppress("OPT_IN_USAGE")
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    override suspend fun accountStates(request: TonCenterAccountStatesRequest): TonCenterAccountStatesResponse {
        return json.decodeFromString<TonCenterAccountStatesResponse>(
            httpClient.get("$endpoint/api/v3/accountStates") {
                contentType(ContentType.Application.Json)
                request.address.forEach {
                    parameter("address", it)
                }
                parameter("include_boc", request.includeBoc)
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
}
