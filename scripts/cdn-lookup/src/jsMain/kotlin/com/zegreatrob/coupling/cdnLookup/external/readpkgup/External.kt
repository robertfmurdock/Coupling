package com.zegreatrob.coupling.cdnLookup.external.readpkgup

import kotlin.js.Json
import kotlin.js.Promise

@JsModule("read-pkg-up")
external val readPkgUp: (json: Json) -> Promise<Json>
