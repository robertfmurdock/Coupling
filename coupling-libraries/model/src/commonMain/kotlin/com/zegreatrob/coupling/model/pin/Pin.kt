package com.zegreatrob.coupling.model.pin

import com.zegreatrob.coupling.model.party.PartyElement

data class Pin(
    val id: String? = null,
    val name: String = "",
    val icon: String = "",
    val target: PinTarget = PinTarget.Pair
)

val PartyElement<Pin>.partyId get() = id
val PartyElement<Pin>.pin get() = element
