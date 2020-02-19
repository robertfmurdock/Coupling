package com.zegreatrob.coupling.model.pin

import com.zegreatrob.coupling.model.tribe.TribeElement

data class Pin(
    val _id: String? = null,
    val name: String? = null,
    val icon: String? = null,
    val target: PinTarget = PinTarget.Pair
)

typealias TribeIdPin = TribeElement<Pin>

val TribeIdPin.tribeId get() = id
val TribeIdPin.pin get() = element