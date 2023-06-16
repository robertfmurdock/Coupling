package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.SlackTeamAccess
import com.zegreatrob.coupling.repository.slack.SlackAccessRepository

class MemorySlackRepository : SlackAccessRepository {
    override suspend fun save(slackTeamAccess: SlackTeamAccess) = Unit
    override suspend fun get(teamId: String): Record<SlackTeamAccess>? = null
}
