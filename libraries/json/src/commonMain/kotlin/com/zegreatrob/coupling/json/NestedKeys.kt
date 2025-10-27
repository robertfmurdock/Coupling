package com.zegreatrob.coupling.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

val jsonWithDefaults = Json { encodeDefaults = true }

inline fun <reified T> T.nestedKeys() = let(jsonWithDefaults::encodeToJsonElement)
    .jsonObject
    .nestedKeys()

data class Entry(val content: Map<String, Entry?>)

fun JsonObject.nestedKeys(): Map<String, Entry?> = keys.mapNotNull { key ->
    when (val entry = jsonObject[key]) {
        is JsonObject -> key to Entry(entry.nestedKeys())
        is JsonArray -> entry.jsonArray.let {
            key to (if (it.isNotEmpty() && it[0] is JsonObject) Entry(it[0].jsonObject.nestedKeys()) else null)
        }

        JsonNull, null -> null
        else -> key to null
    }
}.toMap()

fun Map<String, Entry?>.toGqlQueryFields(): String = if (isEmpty()) {
    ""
} else {
    toQueryLines().joinToString(", ")
        .let { "{ $it }" }
}

fun Map<String, Entry?>.toQueryLines(): List<String> = map { (key, value) ->
    if (value == null) {
        key
    } else {
        "$key ${value.content.toGqlQueryFields()} "
    }
}
