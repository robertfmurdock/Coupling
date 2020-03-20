package com.zegreatrob.coupling.model.pin

import com.zegreatrob.coupling.model.tribe.TribeElement

data class Pin(
    val _id: String? = null,
    val name: String = "",
    val icon: String = "",
    val target: PinTarget = PinTarget.Pair
)

val defaultPin = Pin(_id = "DEFAULT")

typealias TribeIdPin = TribeElement<Pin>

val TribeIdPin.tribeId get() = id
val TribeIdPin.pin get() = element