package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.json.GqlContributionInput
import com.zegreatrob.coupling.json.GqlSaveContributionInput
import com.zegreatrob.coupling.model.ContributionInput
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkSaveContributionCommandDispatcher :
    SaveContributionCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: SaveContributionCommand): VoidResult {
        doQuery(Mutation.saveContribution, command.saveContributionInput())
        return VoidResult.Accepted
    }
}

private fun SaveContributionCommand.saveContributionInput() = GqlSaveContributionInput(
    partyId = partyId.value.toString(),
    contributionList = contributionList.map(ContributionInput::toJson),
)

private fun ContributionInput.toJson() = GqlContributionInput(
    commitCount = commitCount,
    contributionId = contributionId,
    cycleTime = cycleTime,
    dateTime = dateTime,
    ease = ease,
    firstCommit = firstCommit,
    firstCommitDateTime = firstCommitDateTime,
    hash = hash,
    integrationDateTime = integrationDateTime,
    label = label,
    link = link,
    name = name,
    participantEmails = participantEmails.toList(),
    semver = semver,
    story = story,
)
