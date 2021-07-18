package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.fromJsonDynamic
import com.zegreatrob.coupling.json.toJsonDynamic
import com.zegreatrob.minjson.at
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.Window
import org.w3c.dom.get
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

interface GqlSyntax : AxiosSyntax {

    val gqlEndpoint get() = "${basename()}/api/graphql"

    fun basename(): dynamic = if (js("global.window").unsafeCast<Window?>() != null) window["basename"] else ""

    suspend fun String.performQuery() = axios.post(gqlEndpoint, json("query" to this)).handleException().await()

    suspend fun performQuery(body: Json): dynamic = axios.post(gqlEndpoint, body).handleException().await()

    private inline fun Promise<dynamic>.handleException(): Promise<dynamic> = this.catch {
        val responseBody = JSON.stringify(it.unsafeCast<Json>().at("response/data"))
        throw Exception("Axios error. Underlying message: ${it.message}. Body: $responseBody")
    }

}

suspend inline fun <reified T> GqlSyntax.performQuery(query: String, input: T): dynamic = performQuery(
    json("query" to query, "variables" to json("input" to input.toJsonDynamic()))
)

suspend inline fun <reified I, reified O, M> GqlSyntax.performQuery(
    mutation: String,
    input: I,
    resultName: String,
    toOutput: (O) -> M
): M? = performQuery(mutation, input).unsafeCast<Json>()
    .at<Json>("/data/data")!!
    .at<Json>("/$resultName")
    ?.fromJsonDynamic<O>()
    ?.let(toOutput)

object EndpointFinder {
    val gqlEndpoint get() = "${basename()}/api/graphql"

    fun basename(): dynamic = if (js("global.window").unsafeCast<Window?>() != null) window["basename"] else ""
}
