package com.zegreatrob.coupling.server.entity.contribution

import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.perform
import com.zegreatrob.coupling.json.SaveContributionInput
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val saveContributionResolver = dispatch(
    dispatcherFunc = DispatcherProviders.prereleaseCommand(),
    commandFunc = { _: JsonNull, args: SaveContributionInput -> args.toCommand() },
    fireFunc = ::perform,
    toSerializable = { true },
)

private fun SaveContributionInput.toCommand(): SaveContributionCommand = SaveContributionCommand(
    partyId = PartyId(partyId),
    contributionId = contributionId,
    participantEmails = participantEmails,
    hash = hash,
    dateTime = dateTime,
    ease = ease,
    story = story,
    link = link,
)
