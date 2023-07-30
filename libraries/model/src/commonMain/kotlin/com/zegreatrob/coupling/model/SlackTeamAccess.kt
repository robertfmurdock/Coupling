package com.zegreatrob.coupling.model

data class SlackTeamAccess(
    val teamId: String,
    val accessToken: String,
    val appId: String,
    val slackUserId: String,
    val slackBotUserId: String,
)

data class DiscordTeamAccess(
    val webhook: DiscordWebhook,
    val accessToken: String,
    val refreshToken: String,
)

data class DiscordWebhook(val id: String, val token: String)
