package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.schema.fragment.BoostDetailsFragment
import com.zegreatrob.coupling.model.Boost

fun BoostDetailsFragment.toModel(): Boost = Boost(
    userId = userId,
    partyIds = partyIds.toSet(),
    expirationDate = expirationDate,
)
