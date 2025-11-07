package com.zegreatrob.coupling.sdk

import com.apollographql.apollo.api.Optional.Companion.presentIfNotNull
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
import com.zegreatrob.coupling.sdk.schema.SavePairAssignmentsMutation
import com.zegreatrob.coupling.sdk.schema.type.PinInput
import com.zegreatrob.coupling.sdk.schema.type.PinnedPairInput
import com.zegreatrob.coupling.sdk.schema.type.PinnedPlayerInput
import com.zegreatrob.coupling.sdk.schema.type.SavePairAssignmentsInput

interface SdkSavePairAssignmentsCommandDispatcher :
    SavePairAssignmentsCommand.Dispatcher,
    GqlTrait {

    override suspend fun perform(command: SavePairAssignmentsCommand) = with(command) {
        SavePairAssignmentsMutation(partyId.with(pairAssignments).toSavePairAssignmentsInput()).execute()
        VoidResult.Accepted
    }
}

internal fun PartyElement<PairAssignmentDocument>.toSavePairAssignmentsInput() = SavePairAssignmentsInput(
    partyId = partyId,
    pairingSetId = element.id,
    date = element.date,
    pairs = element.pairs.map(PinnedCouplingPair::toSerializableInput).toList(),
    discordMessageId = presentIfNotNull(element.discordMessageId),
    slackMessageId = presentIfNotNull(element.slackMessageId),
)

internal fun PinnedCouplingPair.toSerializableInput() = PinnedPairInput(
    players = pinnedPlayers.map(PinnedPlayer::toSerializableInput).toList(),
    pins = pins.map(Pin::toSerializableInput),
)

internal fun PinnedPlayer.toSerializableInput() = PinnedPlayerInput(
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

internal fun Badge.toSerializable() = when (this) {
    Badge.Default -> com.zegreatrob.coupling.sdk.schema.type.Badge.Default
    Badge.Alternate -> com.zegreatrob.coupling.sdk.schema.type.Badge.Alternate
}

internal fun AvatarType.toSerializable() = name.let { com.zegreatrob.coupling.sdk.schema.type.AvatarType.safeValueOf(it) }

internal fun Pin.toSerializableInput() = PinInput(
    id = id,
    name = presentIfNotNull(name),
    icon = presentIfNotNull(icon),
)
