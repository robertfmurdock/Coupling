@file:JsModule("passport-local")

package com.zegreatrob.coupling.server.external.passportlocal

import com.zegreatrob.coupling.server.external.express.Request
import kotlin.js.Json

external class Strategy(options: Json, function: (Request, String, String, (dynamic, dynamic) -> Unit) -> Unit)
