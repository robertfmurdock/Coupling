package com.zegreatrob.coupling.model.party

import kotlinx.datetime.Instant

data class Secret(val id: String, val description: String, val createdTimestamp: Instant, val lastUsedTimestamp: Instant?)

data class SecretUsed(val partyId: PartyId, val secretId: String, val lastUsedTimestamp: Instant)
