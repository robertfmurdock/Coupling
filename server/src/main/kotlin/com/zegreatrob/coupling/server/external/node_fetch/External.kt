@file:Suppress("unused")

package com.zegreatrob.coupling.server.external.node_fetch

import kotlin.js.Promise

@JsModule("node-fetch")
external fun fetch(url: String): Promise<FetchResult>

external interface FetchResult {
    fun text(): Promise<String>
}
