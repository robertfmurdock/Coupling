package com.zegreatrob.coupling.repository.party

import com.zegreatrob.coupling.model.party.PartyId

fun interface PartyConfigurationBackfill {
    suspend fun backfill(stage: String, partyId: PartyId, runId: String): PartyConfigurationBackfillResult
}

data class PartyConfigurationBackfillResult(
    val importedCount: Int,
    val checkpoint: String?,
    val complete: Boolean,
)
