package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.AvatarType
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.defaultPlayer
import kotools.types.text.NotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi

fun PartyRecord<Player>.toSerializable() = GqlPlayerDetails(
    id = data.element.id.value.toString(),
    name = data.element.name,
    email = data.element.email,
    badge = "${data.element.badge}",
    callSignAdjective = data.element.callSignAdjective,
    callSignNoun = data.element.callSignNoun,
    imageURL = data.element.imageURL,
    avatarType = data.element.avatarType?.toSerializable(),
    unvalidatedEmails = data.element.additionalEmails.toList(),
    partyId = data.partyId.value,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlSavePlayerInput.toModel(): Player = Player(
    id = PlayerId(NotBlankString.create(playerId)),
    badge = badge?.toIntOrNull() ?: defaultPlayer.badge,
    name = name,
    email = email,
    callSignAdjective = callSignAdjective,
    callSignNoun = callSignNoun,
    imageURL = imageURL,
    avatarType = avatarType?.let(AvatarType::valueOf),
    additionalEmails = unvalidatedEmails.toSet(),
)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlPlayerDetails.toModel(): PartyRecord<Player> = PartyRecord(
    PartyId(NotBlankString.create(partyId)).with(
        Player(
            id = PlayerId(NotBlankString.create(id)),
            badge = badge?.toIntOrNull() ?: defaultPlayer.badge,
            name = name,
            email = email,
            callSignAdjective = callSignAdjective,
            callSignNoun = callSignNoun,
            imageURL = imageURL,
            avatarType = avatarType?.toModel(),
            additionalEmails = unvalidatedEmails.toSet(),
        ),
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
