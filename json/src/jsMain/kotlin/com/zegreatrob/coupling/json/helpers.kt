package com.zegreatrob.coupling.json

import kotlin.js.Json

fun Json.getKeys() = js("Object").keys(this).unsafeCast<Array<String>>()