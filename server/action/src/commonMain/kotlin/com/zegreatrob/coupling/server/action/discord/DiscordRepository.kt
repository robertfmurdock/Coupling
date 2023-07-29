package com.zegreatrob.coupling.server.action.discord

interface DiscordRepository {
    suspend fun exchangeForWebhook(code: String): ExchangeResult

    sealed interface ExchangeResult {
        data class Success(val webhook: DiscordWebhook) : ExchangeResult
        data class Error(val error: String, val description: String) : ExchangeResult
    }
}

data class DiscordWebhook(val id: String, val token: String)
