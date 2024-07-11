package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.ClearContributionsCommand
import com.zegreatrob.coupling.repository.contribution.ContributionDeleteAll

interface ServerClearContributionsCommandDispatcher : ClearContributionsCommand.Dispatcher {
    val contributionRepository: ContributionDeleteAll
    override suspend fun perform(command: ClearContributionsCommand): VoidResult {
        contributionRepository.deleteAll(command.partyId)
        return VoidResult.Accepted
    }
}
