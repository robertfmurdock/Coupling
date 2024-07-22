package com.zegreatrob.coupling.server.entity.contribution

import com.zegreatrob.coupling.action.party.ClearContributionsCommand
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.perform
import com.zegreatrob.coupling.json.ClearContributionsInput
import com.zegreatrob.coupling.json.JsonContributionInput
import com.zegreatrob.coupling.json.SaveContributionInput
import com.zegreatrob.coupling.model.ContributionInput
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val saveContributionResolver = dispatch(
    dispatcherFunc = DispatcherProviders.command(),
    commandFunc = requiredInput { _: JsonNull, args: SaveContributionInput -> args.toCommand() },
    fireFunc = ::perform,
    toSerializable = { true },
)

private fun SaveContributionInput.toCommand() = SaveContributionCommand(
    partyId = PartyId(partyId),
    contributionList = contributionList.map(JsonContributionInput::contributionInput),
)

private fun JsonContributionInput.contributionInput() = ContributionInput(
    contributionId = contributionId,
    participantEmails = participantEmails,
    hash = hash,
    dateTime = dateTime,
    ease = ease,
    story = story,
    link = link,
    semver = semver,
    label = label,
    firstCommit = firstCommit,
    firstCommitDateTime = firstCommitDateTime,
    integrationDateTime = integrationDateTime,
    cycleTime = cycleTime,

)

val clearContributionsResolver = dispatch(
    dispatcherFunc = DispatcherProviders.command(),
    commandFunc = requiredInput { _: JsonNull, args: ClearContributionsInput -> args.toCommand() },
    fireFunc = ::perform,
    toSerializable = { true },
)

private fun ClearContributionsInput.toCommand() = ClearContributionsCommand(
    partyId = PartyId(partyId),
)
