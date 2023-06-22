package com.zegreatrob.coupling.model

data class SlackTeamAccess(
    val teamId: String,
    val accessToken: String,
    val appId: String,
    val slackUserId: String,
    val slackBotUserId: String,
)
