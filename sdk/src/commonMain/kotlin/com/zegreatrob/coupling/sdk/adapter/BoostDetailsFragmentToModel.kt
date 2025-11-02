package com.zegreatrob.coupling.sdk.adapter

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.sdk.schema.fragment.BoostDetails

fun BoostDetails.toModel(): Boost = Boost(
    userId = userId,
    partyIds = partyIds.toSet(),
    expirationDate = expirationDate,
)
