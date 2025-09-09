package com.zegreatrob.coupling.sdk

import com.apollographql.apollo.api.Optional.Companion.presentIfNotNull
import com.example.SavePairAssignmentsMutation
import com.example.type.PinInput
import com.example.type.PinnedPairInput
import com.example.type.PinnedPlayerInput
import com.example.type.SavePairAssignmentsInput
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.AvatarType
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkSavePairAssignmentsCommandDispatcher :
    SavePairAssignmentsCommand.Dispatcher,
    GqlTrait {

    override suspend fun perform(command: SavePairAssignmentsCommand) = with(command) {
        apolloMutation(SavePairAssignmentsMutation(partyId.with(pairAssignments).toSavePairAssignmentsInput()))
        VoidResult.Accepted
    }
}

fun PartyElement<PairAssignmentDocument>.toSavePairAssignmentsInput() = SavePairAssignmentsInput(
    partyId = partyId,
    pairAssignmentsId = element.id,
    date = element.date,
    pairs = element.pairs.map(PinnedCouplingPair::toSerializableInput).toList(),
    discordMessageId = presentIfNotNull(element.discordMessageId),
    slackMessageId = presentIfNotNull(element.slackMessageId),
)

fun PinnedCouplingPair.toSerializableInput() = PinnedPairInput(
    players = pinnedPlayers.map(PinnedPlayer::toSerializableInput).toList(),
    pins = pins.map(Pin::toSerializableInput),
)

fun PinnedPlayer.toSerializableInput() = PinnedPlayerInput(
    id = player.id,
    name = player.name,
    email = player.email,
    badge = presentIfNotNull(player.badge.toSerializable()),
    callSignAdjective = player.callSignAdjective,
    callSignNoun = player.callSignNoun,
    imageURL = presentIfNotNull(player.imageURL),
    avatarType = presentIfNotNull(player.avatarType?.toSerializable()),
    unvalidatedEmails = player.additionalEmails.toList(),
    pins = pins.map(Pin::toSerializableInput),
)

fun Badge.toSerializable() = when (this) {
    Badge.Default -> com.example.type.Badge.Default
    Badge.Alternate -> com.example.type.Badge.Alternate
}

fun AvatarType.toSerializable() = name.let { com.example.type.AvatarType.safeValueOf(it) }

fun Pin.toSerializableInput() = PinInput(
    id = id,
    name = presentIfNotNull(name),
    icon = presentIfNotNull(icon),
)
