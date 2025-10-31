package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.sdk.schema.fragment.PartySecretFragment

fun PartySecretFragment.toModel(): Secret = Secret(
    id = id,
    description = description,
    createdTimestamp = createdTimestamp,
    lastUsedTimestamp = lastUsedTimestamp,
)
