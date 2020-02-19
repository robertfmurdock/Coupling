package com.zegreatrob.coupling.model.player

import com.zegreatrob.coupling.model.tribe.TribeElement

data class Player(
    val id: String? = null,
    val badge: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val callSignAdjective: String? = null,
    val callSignNoun: String? = null,
    val imageURL: String? = null
)

typealias TribeIdPlayer = TribeElement<Player>

val TribeIdPlayer.tribeId get() = id
val TribeIdPlayer.player get() = element