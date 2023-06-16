package com.zegreatrob.coupling.repository.slack

import com.zegreatrob.coupling.model.SlackTeamAccess

interface SlackAccessSave {
    suspend fun save(slackTeamAccess: SlackTeamAccess)
}
