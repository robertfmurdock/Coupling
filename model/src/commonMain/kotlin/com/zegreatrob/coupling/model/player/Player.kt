package com.zegreatrob.coupling.model.player

import com.zegreatrob.coupling.model.tribe.TribeElement

data class Player(
    val id: String? = null,
    val badge: Int = Badge.Default.value,
    val name: String = "",
    val email: String = "",
    val callSignAdjective: String = "",
    val callSignNoun: String = "",
    val imageURL: String? = null
)

val defaultPlayer = Player(id = "DEFAULT")

typealias TribeIdPlayer = TribeElement<Player>

val TribeIdPlayer.tribeId get() = id
val TribeIdPlayer.player get() = element