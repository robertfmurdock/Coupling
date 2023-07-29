package com.zegreatrob.coupling.server.discord

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface AccessResponse

@Serializable
data class SuccessfulAccessResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("refresh_token")
    val refreshToken: String,
    val scope: String,
    val webhook: WebhookInformation,
) : AccessResponse
