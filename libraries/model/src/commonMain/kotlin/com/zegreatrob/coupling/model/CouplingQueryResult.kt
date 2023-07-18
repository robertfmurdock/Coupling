package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.user.User

data class CouplingQueryResult(
    val partyList: List<Record<PartyDetails>>? = null,
    val user: User? = null,
    val party: Party? = null,
    val globalStats: GlobalStats? = null,
    val addToSlackUrl: String? = null,
)
