package com.zegreatrob.coupling.model.player

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.party.PartyElement

data class Player(
    val id: String = "${uuid4()}",
    val badge: Int = Badge.Default.value,
    val name: String = "",
    val email: String = "",
    val callSignAdjective: String = "",
    val callSignNoun: String = "",
    val imageURL: String? = null
)

val defaultPlayer = Player(id = "DEFAULT")

val PartyElement<Player>.partyId get() = id
val PartyElement<Player>.player get() = element
