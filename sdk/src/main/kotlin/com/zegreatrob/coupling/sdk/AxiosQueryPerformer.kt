package com.zegreatrob.coupling.sdk

import com.zegreatrob.minjson.at
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

interface AxiosQueryPerformer : QueryPerformer, AxiosSyntax {
    override suspend fun doQuery(body: String): dynamic =
        axios.post(gqlEndpoint, json("query" to body)).handleException().await()

    override suspend fun doQuery(body: Json): dynamic = axios.post(gqlEndpoint, body).handleException().await()

    private inline fun Promise<dynamic>.handleException(): Promise<dynamic> = this.catch {
        val responseBody = JSON.stringify(it.unsafeCast<Json>().at("response/data"))
        throw Exception("Axios error. Underlying message: ${it.message}. Body: $responseBody")
    }

    override fun <T> postAsync(body: dynamic): Deferred<T> = axios.post(gqlEndpoint, body)
        .then<T> { it.data.unsafeCast<T>() }
        .asDeferred()
        .unsafeCast<Deferred<T>>()

}