package com.zegreatrob.coupling.model.pin

import com.zegreatrob.coupling.model.party.PartyElement
import kotools.types.text.NotBlankString

data class Pin(
    val id: NotBlankString,
    val name: String = "",
    val icon: String = "",
    val target: PinTarget = PinTarget.Pair,
)

val PartyElement<Pin>.pin get() = element
