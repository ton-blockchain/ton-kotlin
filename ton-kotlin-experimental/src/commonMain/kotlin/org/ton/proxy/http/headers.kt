package org.ton.proxy.http

import io.ktor.http.*
import org.ton.kotlin.adnl.http.HttpHeader

fun Iterable<HttpHeader>.ktor() = Headers.build {
    this@ktor.forEach {
        append(it.name, it.value)
    }
}

fun Headers.ton() = entries().map { (key, values) ->
    values.map { value ->
        HttpHeader(key, value)
    }
}.flatten()
