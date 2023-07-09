@file:JsModule("node-fetch")
@file:Suppress("unused")

package com.zegreatrob.coupling.server.external.nodefetch

import kotlin.js.Json
import kotlin.js.Promise

@JsName("default")
external fun fetch(url: String, options: Json = definedExternally): Promise<FetchResult>
