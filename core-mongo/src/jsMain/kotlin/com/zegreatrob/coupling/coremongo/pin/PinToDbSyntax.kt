package com.zegreatrob.coupling.coremongo.pin

import com.zegreatrob.coupling.core.entity.pin.Pin
import kotlin.js.Json
import kotlin.js.json

interface PinToDbSyntax {

    fun Array<Json>.toDbPins() = map { it.fromDbToPin() }

    fun Json.fromDbToPin() = Pin(
        _id = stringValue("id") ?: stringValue("_id"),
        name = this["name"]?.toString(),
        tribe = this["tribe"]?.toString()
    )

    fun Pin.toDbJson() = json("id" to _id, "tribe" to tribe, "name" to name)

    private fun Json.stringValue(key: String) = this[key]?.toString()
}
