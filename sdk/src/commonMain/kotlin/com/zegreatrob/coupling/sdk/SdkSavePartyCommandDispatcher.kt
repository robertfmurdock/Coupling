package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.json.SavePartyInput
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.sdk.gql.GqlSyntax
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkSavePartyCommandDispatcher : SavePartyCommand.Dispatcher, GqlSyntax {
    override suspend fun perform(command: SavePartyCommand): VoidResult {
        doQuery(Mutation.saveParty, command.party.savePartyInput())
        return VoidResult.Accepted
    }
}
private fun Party.savePartyInput() = SavePartyInput(
    partyId = id,
    pairingRule = PairingRule.toValue(pairingRule),
    name = name,
    email = email,
    defaultBadgeName = defaultBadgeName,
    alternateBadgeName = alternateBadgeName,
    badgesEnabled = badgesEnabled,
    callSignsEnabled = callSignsEnabled,
    animationsEnabled = animationEnabled,
    animationSpeed = animationSpeed,
    slackChannel = slackChannel,
)
