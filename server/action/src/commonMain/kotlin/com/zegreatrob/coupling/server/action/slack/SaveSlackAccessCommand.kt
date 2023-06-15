package com.zegreatrob.coupling.server.action.slack

import com.zegreatrob.coupling.model.SlackTeamAccess
import com.zegreatrob.coupling.repository.slack.SlackSave
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SaveSlackAccessCommand(val slackTeamAccess: SlackTeamAccess) :
    SimpleSuspendAction<SaveSlackAccessCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {

        val slackRepository: SlackSave

        suspend fun perform(command: SaveSlackAccessCommand) {
            slackRepository.save(command.slackTeamAccess)
        }
    }
}
