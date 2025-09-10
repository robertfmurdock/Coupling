package com.zegreatrob.coupling.sdk

import com.apollographql.apollo.api.Optional
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.model.ContributionInput
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.SaveContributionMutation
import com.zegreatrob.coupling.sdk.schema.type.SaveContributionInput

interface SdkSaveContributionCommandDispatcher :
    SaveContributionCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: SaveContributionCommand): VoidResult {
        SaveContributionMutation(command.saveContributionInput()).execute()
            .dataAssertNoErrors
        return VoidResult.Accepted
    }
}

private fun SaveContributionCommand.saveContributionInput() = SaveContributionInput(
    partyId = partyId,
    contributionList = contributionList.map(ContributionInput::toJson),
)

private fun ContributionInput.toJson() = com.zegreatrob.coupling.sdk.schema.type.ContributionInput(
    contributionId = contributionId,
    commitCount = Optional.present(commitCount),
    cycleTime = Optional.present(cycleTime),
    dateTime = Optional.present(dateTime),
    ease = Optional.present(ease),
    firstCommit = Optional.present(firstCommit),
    firstCommitDateTime = Optional.present(firstCommitDateTime),
    hash = Optional.present(hash),
    integrationDateTime = Optional.present(integrationDateTime),
    label = Optional.present(label),
    link = Optional.present(link),
    name = Optional.present(name),
    participantEmails = participantEmails.toList(),
    semver = Optional.present(semver),
    story = Optional.present(story),
)
