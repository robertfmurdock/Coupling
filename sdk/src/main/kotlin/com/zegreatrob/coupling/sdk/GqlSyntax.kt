package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.fromJsonDynamic
import com.zegreatrob.coupling.json.toJsonDynamic
import com.zegreatrob.minjson.at
import kotlinx.browser.window
import kotlinx.coroutines.Deferred
import org.w3c.dom.Window
import org.w3c.dom.get
import kotlin.js.Json
import kotlin.js.json

interface GqlSyntax {

    val performer: QueryPerformer

    suspend fun String.performQuery(): dynamic = performer.doQuery(this)

    suspend fun performQuery(body: Json): dynamic = performer.doQuery(body)

}

interface QueryPerformer {
    val gqlEndpoint get() = "${basename()}/api/graphql"

    fun basename(): dynamic = if (js("global.window").unsafeCast<Window?>() != null) window["basename"] else ""

    suspend fun doQuery(body: String): dynamic
    suspend fun doQuery(body: Json): dynamic
    fun <T> postAsync(body: dynamic): Deferred<T>

}

suspend inline fun <reified T> GqlSyntax.doQuery(query: String, input: T): dynamic = performQuery(
    json("query" to query, "variables" to json("input" to input.toJsonDynamic()))
)

suspend inline fun <reified I, reified O, M> GqlSyntax.doQuery(
    mutation: String,
    input: I,
    resultName: String,
    toOutput: (O) -> M
): M? = doQuery(mutation, input).unsafeCast<Json>()
    .at<Json>("/data/data")!!
    .at<Json>("/$resultName")
    ?.fromJsonDynamic<O>()
    ?.let(toOutput)

object EndpointFinder {
    val gqlEndpoint get() = "${basename()}/api/graphql"

    fun basename(): dynamic = if (js("global.window").unsafeCast<Window?>() != null) window["basename"] else ""
}
