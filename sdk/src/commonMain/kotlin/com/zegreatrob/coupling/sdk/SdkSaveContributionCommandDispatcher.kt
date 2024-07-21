package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.json.SaveContributionInput
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

private fun SaveContributionCommand.saveContributionInput() = SaveContributionInput(
    partyId = partyId.value,
    contributionId = contributionId,
    hash = hash,
    dateTime = dateTime,
    ease = ease,
    story = story,
    link = link,
    participantEmails = participantEmails,
    semver = semver,
    label = label,
    firstCommit = firstCommit,
    firstCommitDateTime = firstCommitDateTime,
    integrationDateTime = integrationDateTime,
    cycleTime = cycleTime,
)
