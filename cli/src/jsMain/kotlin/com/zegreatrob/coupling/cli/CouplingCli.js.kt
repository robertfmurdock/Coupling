package com.zegreatrob.coupling.cli

import com.zegreatrob.coupling.cli.external.jwtdecode.jwtDecode
import js.objects.Object.Companion.keys

@OptIn(ExperimentalWasmJsInterop::class)
actual fun decodeJwt(accessToken: String): Map<String, String> = runCatching {
    val json = jwtDecode(accessToken)
    keys(json).associateWith {
        json[it].toString()
    }
}.getOrNull() ?: emptyMap()
