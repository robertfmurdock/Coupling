package com.zegreatrob.coupling.server.action.slack

import com.zegreatrob.coupling.action.GrantSlackAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.repository.slack.SlackAccessSave

interface ServerGrantSlackAccessCommandDispatcher : GrantSlackAccessCommand.Dispatcher {

    val slackRepository: SlackRepository
    val slackAccessRepository: SlackAccessSave

    override suspend fun perform(command: GrantSlackAccessCommand) =
        when (val result = slackRepository.exchangeCodeForAccessToken(command.code)) {
            is SlackGrantAccess.Result.Success -> VoidResult.Accepted.also { slackAccessRepository.save(result.access) }
            is SlackGrantAccess.Result.RemoteError -> VoidResult.Rejected
            is SlackGrantAccess.Result.Unknown -> VoidResult.Rejected.also { result.exception.printStackTrace() }
        }
}
