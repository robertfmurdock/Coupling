package com.zegreatrob.coupling.server.action.discord

import com.zegreatrob.coupling.model.DiscordTeamAccess
import com.zegreatrob.coupling.model.DiscordWebhook
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument

interface DiscordRepository : DiscordSendSpin {
    suspend fun exchangeForWebhook(code: String): ExchangeResult

    sealed interface ExchangeResult {
        data class Success(val discordTeamAccess: DiscordTeamAccess) : ExchangeResult
        data class Error(val error: String, val description: String?) : ExchangeResult
    }
}

interface DiscordSendSpin {
    suspend fun sendSpinMessage(webhook: DiscordWebhook, newPairs: PairAssignmentDocument)
}
