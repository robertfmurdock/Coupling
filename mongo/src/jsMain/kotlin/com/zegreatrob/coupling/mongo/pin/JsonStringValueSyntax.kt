package com.zegreatrob.coupling.mongo.pin

import kotlin.js.Json

interface JsonStringValueSyntax {
    fun Json.stringValue(key: String) = this[key]?.toString()
}