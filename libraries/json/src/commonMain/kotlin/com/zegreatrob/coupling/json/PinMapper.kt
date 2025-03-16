package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.pin.Pin

fun Pin.toSerializable() = GqlPin(
    id = id,
    name = name,
    icon = icon,
)

fun Pin.toSerializableInput() = GqlPinInput(
    id = id,
    name = name,
    icon = icon,
)

fun GqlPin.toModel(): Pin? = Pin(
    id = id,
    name = name ?: "",
    icon = icon ?: "",
)

fun GqlPinInput.toModel(): Pin? = Pin(
    id = id,
    name = name ?: "",
    icon = icon ?: "",
)
