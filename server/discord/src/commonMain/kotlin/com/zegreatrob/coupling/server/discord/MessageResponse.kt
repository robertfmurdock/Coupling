package com.zegreatrob.coupling.server.discord

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

sealed interface MessageResponse

@Serializable
data class MessageResponseData(
    val id: String,
    val type: Int,
    val content: String,
    @SerialName("channel_id")
    val channelId: String,
    val author: MessageAuthor,
    val attachments: List<JsonElement>,
    val embeds: List<JsonElement>,
    val mentions: List<JsonElement>,
    @SerialName("mention_roles")
    val mentionRoles: List<JsonElement>,
    val pinned: Boolean,
    @SerialName("mention_everyone")
    val mentionEveryone: Boolean,
    val tts: Boolean,
    val timestamp: String,
    @SerialName("edited_timestamp")
    val editedTimestamp: String?,
    val flags: Int,
    val components: List<JsonElement>,
    @SerialName("application_id")
    val applicationId: String,
    @SerialName("webhook_id")
    val webhookId: String,
) : MessageResponse

@Serializable
data class MessageAuthor(
    val bot: Boolean,
    val id: String,
    val username: String,
    val avatar: String?,
    val discriminator: String?,
)
