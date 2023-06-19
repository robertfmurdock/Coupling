package com.zegreatrob.coupling.server.slack

interface SlackClient {
    suspend fun exchangeCodeForAccess(code: String): AccessResponse

    suspend fun postMessage(
        text: String,
        channel: String,
        accessToken: String,
        blocks: String? = null,
    ): MessageResponse

    suspend fun updateMessage(
        accessToken: String,
        channel: String,
        ts: String?,
        text: String,
        blocks: String? = null,
    ): MessageResponse

    suspend fun getConversationHistory(
        accessToken: String,
        channel: String,
        latest: Double,
        oldest: Double,
    ): HistoryResponse
}
