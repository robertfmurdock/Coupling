package com.zegreatrob.coupling.auth0.management

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceCodeRequest(
    @SerialName("verification_uri_complete")
    val verificationUriComplete: String,
    @SerialName("user_code")
    val userCode: String,
    @SerialName("device_code")
    val deviceCode: String,
    @SerialName("interval")
    val interval: Int,
)
