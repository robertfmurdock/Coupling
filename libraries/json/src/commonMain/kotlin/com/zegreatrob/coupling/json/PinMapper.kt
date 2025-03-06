package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.pin.Pin
import kotools.types.text.toNotBlankString

fun Pin.toSerializable() = GqlPin(
    id = id.toString(),
    name = name,
    icon = icon,
)

fun Pin.toSerializableInput() = GqlPinInput(
    id = id.toString(),
    name = name,
    icon = icon,
)

fun GqlPin.toModel(): Pin? {
    return Pin(
        id = id.toNotBlankString().getOrNull() ?: return null,
        name = name ?: "",
        icon = icon ?: "",
    )
}

fun GqlPinInput.toModel(): Pin? {
    return Pin(
        id = id.toNotBlankString().getOrNull() ?: return null,
        name = name ?: "",
        icon = icon ?: "",
    )
}
