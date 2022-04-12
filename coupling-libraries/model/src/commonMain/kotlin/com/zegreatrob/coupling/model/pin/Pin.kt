package com.zegreatrob.coupling.model.pin

import com.zegreatrob.coupling.model.tribe.PartyElement

data class Pin(
    val id: String? = null,
    val name: String = "",
    val icon: String = "",
    val target: PinTarget = PinTarget.Pair
)

val defaultPin = Pin(id = "DEFAULT")

typealias TribeIdPin = PartyElement<Pin>

val TribeIdPin.tribeId get() = id
val TribeIdPin.pin get() = element