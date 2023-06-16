package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.SlackTeamAccess
import com.zegreatrob.coupling.repository.slack.SlackAccessSave

class MemorySlackRepository : SlackAccessSave {
    override suspend fun save(slackTeamAccess: SlackTeamAccess) {
    }
}
