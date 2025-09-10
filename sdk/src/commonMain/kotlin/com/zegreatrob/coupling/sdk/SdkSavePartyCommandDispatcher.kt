package com.zegreatrob.coupling.sdk

import com.apollographql.apollo.api.Optional.Companion.presentIfNotNull
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.SavePartyMutation
import com.zegreatrob.coupling.sdk.schema.type.SavePartyInput

interface SdkSavePartyCommandDispatcher :
    SavePartyCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: SavePartyCommand): VoidResult {
        SavePartyMutation(command.party.savePartyInput()).execute()
        return VoidResult.Accepted
    }
}

private fun PartyDetails.savePartyInput() = SavePartyInput(
    partyId = id,
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
