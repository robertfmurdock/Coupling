package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.schema.fragment.PartySecretFragment
import com.zegreatrob.coupling.model.party.Secret

fun PartySecretFragment.toModel(): Secret = Secret(
    id = id,
    description = description,
    createdTimestamp = createdTimestamp,
    lastUsedTimestamp = lastUsedTimestamp,
)
