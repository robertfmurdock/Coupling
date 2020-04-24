@file:JsModule("body-parser")
@file:JsNonModule

package com.zegreatrob.coupling.server.external.bodyparser

import com.zegreatrob.coupling.server.external.express.Handler
import kotlin.js.Json

external fun urlencoded(config: Json): Handler
external fun json(): Handler