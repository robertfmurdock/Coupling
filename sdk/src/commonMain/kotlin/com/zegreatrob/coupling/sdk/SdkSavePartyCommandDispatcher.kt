package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.json.SavePartyInput
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.gql.GqlSyntax
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkSavePartyCommandDispatcher : SavePartyCommand.Dispatcher, GqlSyntax {
    override suspend fun perform(command: SavePartyCommand): VoidResult {
        doQuery(Mutation.saveParty, command.party.savePartyInput())
        return VoidResult.Accepted
    }
}
private fun PartyDetails.savePartyInput() = SavePartyInput(
    partyId = id,
    name = name,
    email = email,
    pairingRule = PairingRule.toValue(pairingRule),
    badgesEnabled = badgesEnabled,
    defaultBadgeName = defaultBadgeName,
    alternateBadgeName = alternateBadgeName,
    callSignsEnabled = callSignsEnabled,
    animationsEnabled = animationEnabled,
    animationSpeed = animationSpeed,
)
