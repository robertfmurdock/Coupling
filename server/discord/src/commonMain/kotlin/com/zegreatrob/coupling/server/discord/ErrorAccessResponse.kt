package com.zegreatrob.coupling.server.discord

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorAccessResponse(
    val error: String,
    @SerialName("error_description")
    val errorDescription: String? = null,
) : AccessResponse, MessageResponse
