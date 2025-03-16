package com.zegreatrob.coupling.model.pin

import com.zegreatrob.coupling.model.party.PartyElement
import kotools.types.text.NotBlankString
import kotools.types.text.toNotBlankString
import kotlin.uuid.Uuid

data class PinId(val value: NotBlankString) {
    companion object {
        fun new() = PinId(Uuid.random().toString().toNotBlankString().getOrThrow())
    }
}

data class Pin(
    val id: PinId,
    val name: String = "",
    val icon: String = "",
    val target: PinTarget = PinTarget.Pair,
)

val PartyElement<Pin>.pin get() = element
