package com.zegreatrob.coupling.server.discord

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WebhookInformation(
    val token: String,
    val id: String,
    @SerialName("application_id")
    val applicationId: String,
    val name: String?,
    val url: String,
    @SerialName("channel_id")
    val channelId: String,
    val type: Int,
    val avatar: String?,
    @SerialName("guild_id")
    val guildId: String,
)
