package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.json.GqlSavePartyInput
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkSavePartyCommandDispatcher :
    SavePartyCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: SavePartyCommand): VoidResult {
        doQuery(Mutation.saveParty, command.party.savePartyInput())
        return VoidResult.Accepted
    }
}

private fun PartyDetails.savePartyInput() = GqlSavePartyInput(
    partyId = id.value.toString(),
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
