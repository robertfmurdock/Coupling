package com.zegreatrob.coupling.cdnLookup.external.resolvepkg

import kotlin.js.Json
@JsModule("resolve-pkg")
@JsName("default")
external fun resolvePkg(path: String, options: Json): String?
