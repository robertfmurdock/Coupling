package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.pin.Pin
import kotlinx.serialization.Serializable

@Serializable
data class JsonPin(
    val id: String? = null,
    val name: String = "",
    val icon: String = ""
)

fun Pin.toSerializable() = JsonPin(
    id = id,
    name = name,
    icon = icon,
)

fun JsonPin.toModel(): Pin = Pin(
    id = id,
    name = name,
    icon = icon,
)