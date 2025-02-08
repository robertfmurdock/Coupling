package com.zegreatrob.coupling.cdnLookup.external.resolvepkg

import kotlin.js.Json
import kotlin.js.Promise

@JsModule("resolve-pkg")
external val resolvePkg: (path: String, json: Json) -> Promise<Json>
