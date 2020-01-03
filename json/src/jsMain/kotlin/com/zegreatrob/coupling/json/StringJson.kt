package com.zegreatrob.coupling.json

import kotlin.js.Json

fun Json.stringValue(key: String) = this[key]?.toString()