package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.common.entity.pin.Pin
import kotlin.js.Json
import kotlin.js.json

interface PinToDbSyntax {

    fun Array<Json>.toDbPins() = map { it.toDbPin() }

    fun Json.toDbPin() = Pin(
        _id = this["_id"]?.toString(),
        name = this["name"]?.toString(),
        tribe = this["tribe"]?.toString()
    )

    fun Pin.toDbJson() = json("_id" to _id, "tribe" to tribe, "name" to name)

}
