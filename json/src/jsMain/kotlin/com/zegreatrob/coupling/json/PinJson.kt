package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.pin.Pin
import kotlin.js.Json
import kotlin.js.json

fun List<Pin>.toJson(): Array<Json> = map { it.toJson() }
    .toTypedArray()

fun Pin.toJson() =
    json("_id" to _id, "name" to name, "icon" to icon)

fun List<Pin>.toJsonArray() = map { it.toJson() }
    .toTypedArray()

fun Array<Json>.toPins() = map { it.toPin() }

fun Json.toPin() = Pin(
    _id = this["_id"]?.toString(),
    name = this["name"]?.toString(),
    icon = this["icon"]?.toString()
)

val pinJsonKeys = Pin()
    .toJson()
    .getKeys()