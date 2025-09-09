package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.party.Secret

fun Pair<Secret, String>.toModel() = GqlSecretToken(
    secretId = first.id,
    description = first.description,
    createdTimestamp = first.createdTimestamp,
    lastUsedTimestamp = first.lastUsedTimestamp,
    secretToken = second,
)
