package com.zegreatrob.coupling.repository.slack

import com.zegreatrob.coupling.model.SlackTeamAccess

interface SlackSave {
    suspend fun save(slackTeamAccess: SlackTeamAccess)
}
