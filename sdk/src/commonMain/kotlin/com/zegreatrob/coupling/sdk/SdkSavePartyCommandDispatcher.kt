package com.zegreatrob.coupling.sdk

import com.apollographql.apollo.api.Optional.Companion.presentIfNotNull
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.SavePartyMutation
import com.zegreatrob.coupling.sdk.schema.type.SavePartyInput
import com.zegreatrob.coupling.sdk.schema.type.SavePartyPinInput
import com.zegreatrob.coupling.sdk.schema.type.SavePartyPinsInput
import com.zegreatrob.coupling.sdk.schema.type.SavePartyPlayerInput
import com.zegreatrob.coupling.sdk.schema.type.SavePartyPlayersInput

interface SdkSavePartyCommandDispatcher :
    SavePartyCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: SavePartyCommand): VoidResult {
        SavePartyMutation(command.savePartyInput()).execute()
        return VoidResult.Accepted
    }
}

internal fun PartyDetails.savePartyDetailsInput() = com.zegreatrob.coupling.sdk.schema.type.SavePartyDetailsInput(
    name = presentIfNotNull(name),
    email = presentIfNotNull(email),
    pairingRule = presentIfNotNull(PairingRule.toValue(pairingRule)),
    badgesEnabled = presentIfNotNull(badgesEnabled),
    defaultBadgeName = presentIfNotNull(defaultBadgeName),
    alternateBadgeName = presentIfNotNull(alternateBadgeName),
    callSignsEnabled = presentIfNotNull(callSignsEnabled),
    animationsEnabled = presentIfNotNull(animationEnabled),
    animationSpeed = presentIfNotNull(animationSpeed),
)

internal fun SavePartyCommand.savePartyInput() = SavePartyInput(
    partyId = partyId,
    party = presentIfNotNull(party?.savePartyDetailsInput()),
    players = presentIfNotNull(
        if (players.isEmpty()) {
            null
        } else {
            SavePartyPlayersInput(
                items = players.map { it.toSavePartyPlayerInput() },
            )
        },
    ),
    pins = presentIfNotNull(
        if (pins.isEmpty()) {
            null
        } else {
            SavePartyPinsInput(
                items = pins.map { it.toSavePartyPinInput() },
            )
        },
    ),
)

private fun Player.toSavePartyPlayerInput() = SavePartyPlayerInput(
    playerId = id,
    name = name,
    email = email,
    badge = badge.toSerializable(),
    callSignAdjective = callSignAdjective,
    callSignNoun = callSignNoun,
    imageURL = presentIfNotNull(imageURL),
    avatarType = presentIfNotNull(avatarType?.name),
    unvalidatedEmails = additionalEmails.toList(),
)

private fun Pin.toSavePartyPinInput() = SavePartyPinInput(
    pinId = id,
    icon = icon,
    name = name,
)
