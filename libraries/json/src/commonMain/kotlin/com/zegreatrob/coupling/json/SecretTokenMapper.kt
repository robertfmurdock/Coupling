package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.SecretId

fun GqlSecretToken.toDomain(): Pair<Secret, String>? {
    return Secret(
        id = SecretId(secretId) ?: return null,
        description = description,
        createdTimestamp = createdTimestamp,
        lastUsedTimestamp = lastUsedTimestamp,
    ) to secretToken
}

fun Pair<Secret, String>.toModel() = GqlSecretToken(
    secretId = first.id.value.toString(),
    description = first.description,
    createdTimestamp = first.createdTimestamp,
    lastUsedTimestamp = first.lastUsedTimestamp,
    secretToken = second,
)
