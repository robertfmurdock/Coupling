package com.zegreatrob.coupling.server.entity.contribution

import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.perform
import com.zegreatrob.coupling.json.GqlContributionInput
import com.zegreatrob.coupling.json.GqlSaveContributionInput
import com.zegreatrob.coupling.model.ContributionInput
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val saveContributionResolver = dispatch(
    dispatcherFunc = DispatcherProviders.command(),
    commandFunc = requiredInput { _: JsonNull, args: GqlSaveContributionInput -> args.toCommand() },
    fireFunc = ::perform,
    toSerializable = { true },
)

private fun GqlSaveContributionInput.toCommand() = SaveContributionCommand(
    partyId = PartyId(partyId),
    contributionList = contributionList.map(GqlContributionInput::contributionInput),
)

private fun GqlContributionInput.contributionInput() = ContributionInput(
    contributionId = contributionId,
    participantEmails = participantEmails.toSet(),
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
