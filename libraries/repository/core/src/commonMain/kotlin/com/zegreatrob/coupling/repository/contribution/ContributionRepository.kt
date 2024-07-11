package com.zegreatrob.coupling.repository.contribution

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId

fun interface ContributionGet {
    suspend fun get(partyId: PartyId): List<PartyRecord<Contribution>>
}

fun interface ContributionDeleteAll {
    suspend fun deleteAll(partyId: PartyId)
}

fun interface ContributionSave {
    suspend fun save(partyContribution: PartyElement<Contribution>)
}

interface ContributionRepository :
    ContributionGet,
    ContributionSave,
    ContributionDeleteAll
