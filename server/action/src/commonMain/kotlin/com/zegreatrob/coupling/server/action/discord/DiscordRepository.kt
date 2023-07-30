package com.zegreatrob.coupling.server.action.discord

import com.zegreatrob.coupling.model.DiscordTeamAccess

interface DiscordRepository {
    suspend fun exchangeForWebhook(code: String): ExchangeResult

    sealed interface ExchangeResult {
        data class Success(val discordTeamAccess: DiscordTeamAccess) : ExchangeResult
        data class Error(val error: String, val description: String?) : ExchangeResult
    }
}
