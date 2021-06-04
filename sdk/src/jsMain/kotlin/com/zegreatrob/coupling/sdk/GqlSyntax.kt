package com.zegreatrob.coupling.sdk

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.Window
import org.w3c.dom.get
import kotlin.js.Json
import kotlin.js.json

interface GqlSyntax : AxiosSyntax {
    suspend fun String.performQuery() = axios.post(gqlEndpoint, json("query" to this)).await()
    suspend fun performQuery(body: Json) = axios.post(gqlEndpoint, body).await()
}

val gqlEndpoint get() = "${basename()}/api/graphql"

private fun basename(): dynamic = if(js("global.window").unsafeCast<Window?>() != null) window["basename"] else ""
