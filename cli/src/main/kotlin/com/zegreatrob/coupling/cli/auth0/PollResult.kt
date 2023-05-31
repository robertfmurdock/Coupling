package com.zegreatrob.coupling.cli.auth0

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PollResult(
    val error: String? = null,
    @SerialName("access_token")
    val accessToken: String? = null,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("id_token")
    val idToken: String? = null,
    @SerialName("token_type")
    val tokenType: String? = null,
    @SerialName("expires_in")
    val expiresIn: Int? = null,
)
