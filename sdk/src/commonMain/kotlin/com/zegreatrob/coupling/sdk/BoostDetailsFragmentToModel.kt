package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.sdk.schema.fragment.BoostDetailsFragment

fun BoostDetailsFragment.toModel(): Boost = Boost(
    userId = userId,
    partyIds = partyIds.toSet(),
    expirationDate = expirationDate,
)
