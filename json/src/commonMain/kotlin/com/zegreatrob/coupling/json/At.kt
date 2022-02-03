package com.zegreatrob.coupling.json

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

fun JsonElement.at(path: String): JsonElement? = path.split('/')
    .filterNot(String::isEmpty)
    .fold<String, JsonElement?>(this) { accumulator, value ->
        (accumulator as? JsonObject)
            ?.get(value)
    }
