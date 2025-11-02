package com.zegreatrob.coupling.sdk.mapper

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.sdk.schema.fragment.BoostDetails

fun BoostDetails.toDomain(): Boost = Boost(
    userId = userId,
    partyIds = partyIds.toSet(),
    expirationDate = expirationDate,
)
