package com.zegreatrob.coupling.sdk.mapper

import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.sdk.schema.fragment.PartySecret

fun PartySecret.toDomain(): Secret = Secret(
    id = id,
    description = description,
    createdTimestamp = createdTimestamp,
    lastUsedTimestamp = lastUsedTimestamp,
)
