package com.zegreatrob.coupling.json

import kotlinx.serialization.Serializable

@Serializable
data class CreateSecretInput(
    val partyId: String,
    val description: String,
)
