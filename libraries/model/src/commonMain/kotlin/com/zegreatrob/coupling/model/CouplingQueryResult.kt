package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.user.User

data class CouplingQueryResult(
    val partyList: List<Record<Party>>? = null,
    val user: User? = null,
    val partyData: PartyData? = null,
    val globalStats: GlobalStats? = null,
)

data class CouplingMutationResult(
    val createSecret: Pair<Secret, String>? = null,
    val deleteSecret: Boolean? = null,
)
