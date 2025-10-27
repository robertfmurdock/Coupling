package com.zegreatrob.coupling.json

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

val couplingJsonFormat = kotlinx.serialization.json.Json {
    explicitNulls = false
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}

inline fun <reified T> T.toJsonString() = couplingJsonFormat.encodeToString(this)
inline fun <reified T> String.fromJsonString() = couplingJsonFormat.decodeFromString<T>(this)
inline fun <reified T> T.toJsonElement() = couplingJsonFormat.encodeToJsonElement(this)
inline fun <reified T> JsonElement.fromJsonElement() = couplingJsonFormat.decodeFromJsonElement<T>(this)
