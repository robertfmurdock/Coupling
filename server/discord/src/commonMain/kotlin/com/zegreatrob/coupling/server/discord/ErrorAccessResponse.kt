package com.zegreatrob.coupling.server.discord

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorAccessResponse(
    val error: String? = null,
    val message: String? = null,
    @SerialName("error_description")
    val errorDescription: String? = null,
) : AccessResponse,
    MessageResponse
