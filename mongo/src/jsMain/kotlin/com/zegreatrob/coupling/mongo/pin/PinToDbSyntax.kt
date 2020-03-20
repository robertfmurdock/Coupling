package com.zegreatrob.coupling.mongo.pin

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.defaultPin
import kotlin.js.Json
import kotlin.js.json

interface PinToDbSyntax : JsonStringValueSyntax {

    fun Array<Json>.toDbPins() = map { it.fromDbToPin() }

    fun Json.fromDbToPin() = Pin(
        _id = stringValue("id") ?: stringValue("_id"),
        name = this["name"]?.toString() ?: defaultPin.name,
        icon = this["icon"]?.toString() ?: defaultPin.icon
    )

    fun Pin.toDbJson() = json("id" to _id, "name" to name, "icon" to icon)

}
