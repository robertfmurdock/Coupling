package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.user.User

data class CouplingQueryResult(
    val partyList: List<Record<PartyDetails>>? = null,
    val user: User? = null,
    val party: Party? = null,
    val globalStats: GlobalStats? = null,
    val addToSlackUrl: String? = null,
)

data class CouplingMutationResult(
    val createSecret: Pair<Secret, String>? = null,
    val deleteSecret: Boolean? = null,
    val saveSlackIntegration: Boolean?,
)
