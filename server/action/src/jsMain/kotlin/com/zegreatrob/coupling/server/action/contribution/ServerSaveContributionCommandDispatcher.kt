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
                Contribution(
                    id = command.contributionId,
                    createdAt = Clock.System.now(),
                    dateTime = command.dateTime,
                    hash = command.hash,
                    ease = command.ease,
                    story = command.story,
                    link = command.link,
                    participantEmails = command.participantEmails,
                    label = command.label,
                    semver = command.semver,
                    firstCommit = command.firstCommit,
                ),
            ),
        )
        return VoidResult.Accepted
    }
}
