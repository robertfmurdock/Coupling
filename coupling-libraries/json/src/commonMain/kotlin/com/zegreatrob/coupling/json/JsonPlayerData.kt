@file:UseSerializers(DateTimeSerializer::class, PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.AvatarType
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

interface JsonPlayer {
    val id: String
    val name: String
    val email: String
    val badge: String
    val callSignAdjective: String
    val callSignNoun: String
    val imageURL: String?
    val avatarType: String?
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
    override val avatarType: String? = defaultPlayer.avatarType?.name,
) : JsonPlayer

@Serializable
data class SavePlayerInput(
    val playerId: String,
    override val partyId: PartyId,
    val name: String = defaultPlayer.name,
    val email: String = defaultPlayer.email,
    val badge: String = "${defaultPlayer.badge}",
    val callSignAdjective: String = defaultPlayer.callSignAdjective,
    val callSignNoun: String = defaultPlayer.callSignNoun,
    val imageURL: String? = defaultPlayer.imageURL,
    val avatarType: String? = defaultPlayer.avatarType?.name,
) : PartyInput

@Serializable
data class JsonPlayerRecord(
    override val id: String,
    override val name: String = defaultPlayer.name,
    override val email: String = defaultPlayer.email,
    override val badge: String = "${defaultPlayer.badge}",
    override val callSignAdjective: String = defaultPlayer.callSignAdjective,
    override val callSignNoun: String = defaultPlayer.callSignNoun,
    override val imageURL: String? = defaultPlayer.imageURL,
    override val avatarType: String? = defaultPlayer.avatarType?.name,
    override val partyId: PartyId,
    override val modifyingUserEmail: String,
    override val isDeleted: Boolean,
    override val timestamp: DateTime,
) : JsonPartyRecordInfo, JsonPlayer

fun Player.toSerializable() = JsonPlayerData(
    id = id,
    name = name,
    email = email,
    badge = "$badge",
    callSignAdjective = callSignAdjective,
    callSignNoun = callSignNoun,
    imageURL = imageURL,
    avatarType = avatarType?.name,
)

fun PartyRecord<Player>.toSerializable() = JsonPlayerRecord(
    id = data.element.id,
    name = data.element.name,
    email = data.element.email,
    badge = "${data.element.badge}",
    callSignAdjective = data.element.callSignAdjective,
    callSignNoun = data.element.callSignNoun,
    imageURL = data.element.imageURL,
    avatarType = data.element.avatarType?.name,
    partyId = data.id,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun SavePlayerInput.toModel(): Player = Player(
    id = playerId,
    badge = badge.toIntOrNull() ?: defaultPlayer.badge,
    name = name,
    email = email,
    callSignAdjective = callSignAdjective,
    callSignNoun = callSignNoun,
    imageURL = imageURL,
    avatarType = avatarType?.let(AvatarType::valueOf),
)

fun JsonPlayer.toModel(): Player = Player(
    id = id,
    badge = badge.toIntOrNull() ?: defaultPlayer.badge,
    name = name,
    email = email,
    callSignAdjective = callSignAdjective,
    callSignNoun = callSignNoun,
    imageURL = imageURL,
    avatarType = avatarType?.let(AvatarType::valueOf),
)

fun JsonPlayerRecord.toModel(): PartyRecord<Player> = PartyRecord(
    partyId.with(
        Player(
            id = id,
            badge = badge.toIntOrNull() ?: defaultPlayer.badge,
            name = name,
            email = email,
            callSignAdjective = callSignAdjective,
            callSignNoun = callSignNoun,
            imageURL = imageURL,
            avatarType = avatarType?.let(AvatarType::valueOf),
        ),
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
