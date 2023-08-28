package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.user.User

data class CouplingQueryResult(
    val raw: String? = null,
    val partyList: List<Party>? = null,
    val user: User? = null,
    val party: Party? = null,
    val globalStats: GlobalStats? = null,
    val config: CouplingConfig? = null,
)
