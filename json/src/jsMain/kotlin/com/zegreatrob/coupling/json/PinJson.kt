package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.pin.defaultPin
import com.zegreatrob.coupling.model.pin.pin
import kotlin.js.Json
import kotlin.js.json

fun List<Pin>.toJson(): Array<Json> = map { it.toJson() }
    .toTypedArray()

fun Pin.toJson() =
    json("_id" to _id, "name" to name, "icon" to icon)

fun List<Record<TribeIdPin>>.toJsonArray() = map { it.toJson().add(it.data.pin.toJson()) }
    .toTypedArray()

fun Array<Json>.toPins() = map { it.toPin() }

fun Json.toPin() = Pin(
    _id = this["_id"]?.toString(),
    name = this["name"]?.toString() ?: defaultPin.name,
    icon = this["icon"]?.toString() ?: defaultPin.icon
)

val pinJsonKeys
    get() = Pin()
        .toJson()
        .getKeys()

val pinRecordJsonKeys
    get() = pinJsonKeys + recordJsonKeys