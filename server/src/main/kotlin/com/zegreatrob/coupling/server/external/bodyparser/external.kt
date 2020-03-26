@file:JsModule("body-parser")
@file:JsNonModule

package com.zegreatrob.coupling.server.external.bodyparser

import kotlin.js.Json

external fun urlencoded(config: Json): dynamic
external fun json(): dynamic