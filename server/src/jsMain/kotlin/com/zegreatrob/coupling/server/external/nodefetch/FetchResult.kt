package com.zegreatrob.coupling.server.external.nodefetch

import kotlin.js.Promise

external interface FetchResult {
    fun text(): Promise<String>
}
