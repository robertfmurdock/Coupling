package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.contribution.ContributionSave
import kotlinx.datetime.Clock

interface ServerSaveContributionCommandDispatcher : SaveContributionCommand.Dispatcher {
    val contributionRepository: ContributionSave
    override suspend fun perform(command: SaveContributionCommand): VoidResult {
        contributionRepository.save(
            command.partyId.with(
                element =
                command.contributionList.map { input ->
                    Contribution(
                        id = input.contributionId,
                        createdAt = Clock.System.now(),
                        dateTime = input.dateTime,
                        hash = input.hash,
                        ease = input.ease,
                        story = input.story,
                        link = input.link,
                        participantEmails = input.participantEmails,
                        label = input.label,
                        semver = input.semver,
                        firstCommit = input.firstCommit,
                        firstCommitDateTime = input.firstCommitDateTime,
                        integrationDateTime = input.integrationDateTime,
                        cycleTime = input.cycleTime,
                        name = input.name,
                        commitCount = input.commitCount,
                    )
                },
            ),
        )
        return VoidResult.Accepted
    }
}
