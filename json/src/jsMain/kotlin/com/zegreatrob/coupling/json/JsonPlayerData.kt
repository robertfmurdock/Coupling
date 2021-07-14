package com.zegreatrob.coupling.json

import com.soywiz.klock.DateTime
import com.soywiz.klock.js.toDate
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import kotlinx.serialization.Serializable

interface JsonPlayer {
    val id: String
    val name: String
    val email: String
    val badge: String
    val callSignAdjective: String
    val callSignNoun: String
    val imageURL: String?
}

@Serializable
data class JsonPlayerData(
    override val id: String,
    override val name: String = defaultPlayer.name,
    override val email: String = defaultPlayer.email,
    override val badge: String = "${defaultPlayer.badge}",
    override val callSignAdjective: String = defaultPlayer.callSignAdjective,
    override val callSignNoun: String = defaultPlayer.callSignNoun,
    override val imageURL: String? = defaultPlayer.imageURL,
) : JsonPlayer

@Serializable
data class SavePlayerInput(
    val playerId: String,
    val tribeId: String,
    val name: String = defaultPlayer.name,
    val email: String = defaultPlayer.email,
    val badge: String = "${defaultPlayer.badge}",
    val callSignAdjective: String = defaultPlayer.callSignAdjective,
    val callSignNoun: String = defaultPlayer.callSignNoun,
    val imageURL: String? = defaultPlayer.imageURL,
)

@Serializable
data class JsonPlayerRecord(
    override val id: String,
    override val name: String = defaultPlayer.name,
    override val email: String = defaultPlayer.email,
    override val badge: String = "${defaultPlayer.badge}",
    override val callSignAdjective: String = defaultPlayer.callSignAdjective,
    override val callSignNoun: String = defaultPlayer.callSignNoun,
    override val imageURL: String? = defaultPlayer.imageURL,

    override val tribeId: String? = null,
    override val modifyingUserEmail: String? = null,
    override val isDeleted: Boolean? = false,
    override val timestamp: String? = DateTime.now().toDate().toISOString(),
) : JsonTribeRecord, JsonPlayer

fun Player.toSerializable() = JsonPlayerData(
    id = id,
    name = name,
    email = email,
    badge = "$badge",
    callSignAdjective = callSignAdjective,
    callSignNoun = callSignNoun,
    imageURL = imageURL
)

fun TribeRecord<Player>.toSerializable() = JsonPlayerRecord(
    id = data.element.id,
    name = data.element.name,
    email = data.element.email,
    badge = "${data.element.badge}",
    callSignAdjective = data.element.callSignAdjective,
    callSignNoun = data.element.callSignNoun,
    imageURL = data.element.imageURL,
    tribeId = data.id.value,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp.toDate().toISOString(),
)

fun SavePlayerInput.toModel(): Player = Player(
    id = playerId,
    badge = badge.toIntOrNull() ?: defaultPlayer.badge,
    name = name,
    email = email,
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

fun JsonPlayerRecord.toModel(): TribeRecord<Player> = TribeRecord(
    TribeId(tribeId!!).with(
        Player(
            id = id,
            badge = badge.toIntOrNull() ?: defaultPlayer.badge,
            name = name,
            email = email,
            callSignAdjective = callSignAdjective,
            callSignNoun = callSignNoun,
            imageURL = imageURL
        )
    ),
    modifyingUserId = modifyingUserEmail!!,
    isDeleted = isDeleted!!,
    timestamp = DateTime.fromString(timestamp!!).local
)