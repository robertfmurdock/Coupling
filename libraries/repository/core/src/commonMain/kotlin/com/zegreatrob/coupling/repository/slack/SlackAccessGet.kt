package com.zegreatrob.coupling.repository.slack

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.SlackTeamAccess

interface SlackAccessGet {
    suspend fun get(teamId: String): Record<SlackTeamAccess>?
}
