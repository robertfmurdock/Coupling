package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.player.AvatarType
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.defaultPlayer
import kotlinx.serialization.Serializable
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi

interface JsonPlayer {
    val id: String
    val name: String
    val email: String
    val badge: GqlBadge
    val callSignAdjective: String
    val callSignNoun: String
    val imageURL: String?
    val avatarType: String?
    val unvalidatedEmails: Set<String>
}

@Serializable
data class JsonPlayerData(
    override val id: String,
    override val name: String = defaultPlayer.name,
    override val email: String = defaultPlayer.email,
    override val badge: GqlBadge = defaultPlayer.badge.toSerializable(),
    override val callSignAdjective: String = defaultPlayer.callSignAdjective,
    override val callSignNoun: String = defaultPlayer.callSignNoun,
    override val imageURL: String? = defaultPlayer.imageURL,
    override val avatarType: String? = defaultPlayer.avatarType?.name,
    override val unvalidatedEmails: Set<String> = defaultPlayer.additionalEmails,
) : JsonPlayer

fun Player.toSerializable() = JsonPlayerData(
    id = id.value.toString(),
    name = name,
    email = email,
    badge = badge.toSerializable(),
    callSignAdjective = callSignAdjective,
    callSignNoun = callSignNoun,
    imageURL = imageURL,
    avatarType = avatarType?.name,
    unvalidatedEmails = additionalEmails,
)

fun Badge.toSerializable() = when (this) {
    Badge.Default -> GqlBadge.Default
    Badge.Alternate -> GqlBadge.Alternate
}

@OptIn(ExperimentalKotoolsTypesApi::class)
fun JsonPlayer.toModel(): Player = Player(
    id = PlayerId(id.toNotBlankString().getOrThrow()),
    badge = badge.toModel(),
    name = name,
    email = email,
    callSignAdjective = callSignAdjective,
    callSignNoun = callSignNoun,
    imageURL = imageURL,
    avatarType = avatarType.takeUnless(String?::isNullOrEmpty)?.let(AvatarType::valueOf),
    additionalEmails = unvalidatedEmails,
)

fun GqlBadge.toModel() = when (this) {
    GqlBadge.Default -> Badge.Default
    GqlBadge.Alternate -> Badge.Alternate
}
