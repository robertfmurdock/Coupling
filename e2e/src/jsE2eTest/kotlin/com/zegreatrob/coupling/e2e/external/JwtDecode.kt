@file:JsModule("jwt-decode")

package com.zegreatrob.coupling.e2e.external

import kotlin.js.Json

@JsName("jwtDecode")
external fun jwtDecode(token: String): Json
