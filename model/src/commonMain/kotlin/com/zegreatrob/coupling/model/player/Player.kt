package com.zegreatrob.coupling.model.player

import com.zegreatrob.coupling.model.tribe.TribeElement
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with

data class Player(
    val id: String? = null,
    val badge: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val callSignAdjective: String? = null,
    val callSignNoun: String? = null,
    val imageURL: String? = null
)

infix fun Player.with(tribeId: TribeId) = tribeId.with(this)

typealias TribeIdPlayer = TribeElement<Player>

val TribeIdPlayer.tribeId get() = id
val TribeIdPlayer.player get() = element