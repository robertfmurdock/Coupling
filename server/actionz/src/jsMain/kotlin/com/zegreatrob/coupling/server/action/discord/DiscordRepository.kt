package com.zegreatrob.coupling.server.action.discord

import com.zegreatrob.coupling.model.DiscordTeamAccess
import com.zegreatrob.coupling.model.DiscordWebhook
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet

interface DiscordRepository :
    DiscordSendSpin,
    DiscordDeleteSpin {
    suspend fun exchangeForWebhook(code: String): ExchangeResult

    sealed interface ExchangeResult {
        data class Success(val discordTeamAccess: DiscordTeamAccess) : ExchangeResult
        data class Error(val error: String, val description: String?) : ExchangeResult
    }
}

fun interface DiscordSendSpin {
    suspend fun sendSpinMessage(webhook: DiscordWebhook, newPairs: PairingSet): String?
}

fun interface DiscordDeleteSpin {
    suspend fun deleteMessage(webhook: DiscordWebhook, deadPairs: PairingSet)
}
