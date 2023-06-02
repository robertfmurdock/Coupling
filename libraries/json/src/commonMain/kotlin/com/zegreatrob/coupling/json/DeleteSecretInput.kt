package com.zegreatrob.coupling.json

import kotlinx.serialization.Serializable

@Serializable
data class DeleteSecretInput(
    val partyId: String,
    val secretId: String,
)
