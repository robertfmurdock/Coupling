package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.SlackTeamAccess
import com.zegreatrob.coupling.repository.slack.SlackSave

class MemorySlackRepository : SlackSave {
    override suspend fun save(slackTeamAccess: SlackTeamAccess) {
    }
}
