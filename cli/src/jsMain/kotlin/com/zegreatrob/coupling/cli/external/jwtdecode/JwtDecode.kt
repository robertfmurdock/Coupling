@file:JsModule("jwt-decode")

package com.zegreatrob.coupling.cli.external.jwtdecode

import kotlin.js.Json

@JsName("jwtDecode")
external fun jwtDecode(token: String): Json
