package com.zegreatrob.coupling.model.player

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.tribe.PartyElement

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

typealias TribeIdPlayer = PartyElement<Player>

val TribeIdPlayer.tribeId get() = id
val TribeIdPlayer.player get() = element
