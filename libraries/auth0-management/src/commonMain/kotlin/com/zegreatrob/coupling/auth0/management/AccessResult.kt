package com.zegreatrob.coupling.auth0.management

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccessResult(
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
