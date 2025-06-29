package com.zegreatrob.coupling.model.party

import kotlin.time.Instant

data class Secret(
    val id: SecretId,
    val description: String,
    val createdTimestamp: Instant,
    val lastUsedTimestamp: Instant?,
)

data class SecretUsed(val partyId: PartyId, val secretId: SecretId, val lastUsedTimestamp: Instant)
