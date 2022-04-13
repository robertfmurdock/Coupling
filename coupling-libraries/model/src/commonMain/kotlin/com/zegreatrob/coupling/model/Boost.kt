package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.party.PartyId

data class Boost(val userId: String, val partyIds: Set<PartyId>)
