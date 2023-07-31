package com.zegreatrob.coupling.server.action.discord

import com.zegreatrob.coupling.model.DiscordTeamAccess
import com.zegreatrob.coupling.model.DiscordWebhook
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument

interface DiscordRepository {
    suspend fun exchangeForWebhook(code: String): ExchangeResult
    suspend fun sendSpinMessage(webhook: DiscordWebhook, newPairs: PairAssignmentDocument)

    sealed interface ExchangeResult {
        data class Success(val discordTeamAccess: DiscordTeamAccess) : ExchangeResult
        data class Error(val error: String, val description: String?) : ExchangeResult
    }
}
