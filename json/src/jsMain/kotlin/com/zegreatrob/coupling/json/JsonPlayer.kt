package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import kotlinx.serialization.Serializable

@Serializable
data class JsonPlayer(
    val id: String,
    val name: String = defaultPlayer.name,
    val email: String = defaultPlayer.email,
    val badge: String = "${defaultPlayer.badge}",
    val callSignAdjective: String = defaultPlayer.callSignAdjective,
    val callSignNoun: String = defaultPlayer.callSignNoun,
    val imageURL: String? = defaultPlayer.imageURL,
)

fun Player.toSerializable() = JsonPlayer(
    id = id,
    name = name,
    email = email,
    badge = "$badge",
    callSignAdjective = callSignAdjective,
    callSignNoun = callSignNoun,
    imageURL = imageURL
)

fun JsonPlayer.toModel(): Player = Player(
    id = id,
    badge = badge.toIntOrNull() ?: defaultPlayer.badge,
    name = name,
    email = email,
    callSignAdjective = callSignAdjective,
    callSignNoun = callSignNoun,
    imageURL = imageURL
)