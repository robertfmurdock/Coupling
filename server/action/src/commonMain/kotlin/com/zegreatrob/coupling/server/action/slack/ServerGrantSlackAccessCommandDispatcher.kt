package com.zegreatrob.coupling.server.action.slack

import com.zegreatrob.coupling.action.GrantSlackAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.repository.slack.SlackAccessSave

interface ServerGrantSlackAccessCommandDispatcher : GrantSlackAccessCommand.Dispatcher {

    val slackRepository: SlackRepository
    val slackAccessRepository: SlackAccessSave

    override suspend fun perform(command: GrantSlackAccessCommand): VoidResult {
        val access = runCatching { slackRepository.exchangeCodeForAccessToken(command.code) }
        return if (access.isSuccess) {
            slackAccessRepository.save(access.getOrThrow())
            VoidResult.Accepted
        } else {
            access.exceptionOrNull()?.printStackTrace()
            VoidResult.Rejected
        }
    }
}
