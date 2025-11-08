package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.pin.Pin

fun Pin.toSerializable() = GqlPinSnapshot(
    id = id,
    name = name,
    icon = icon,
)

fun Pin.toSerializableInput() = GqlPinInput(
    id = id,
    name = name,
    icon = icon,
)

fun GqlPinSnapshot.toModel(): Pin = Pin(
    id = id,
    name = name,
    icon = icon,
)

fun GqlPinInput.toModel(): Pin = Pin(
    id = id,
    name = name ?: "",
    icon = icon ?: "",
)
