package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.CouplingMutationResult
import com.zegreatrob.coupling.model.party.Secret

fun GqlMutation.toDomain() = CouplingMutationResult(
    createSecret = createSecret?.toDomain(),
    createConnectUserSecret = createConnectUserSecret?.toDomain(),
    deleteSecret = deleteSecret,
    saveSlackIntegration = saveSlackIntegration,
)

fun GqlSecretToken.toDomain(): Pair<Secret, String> = Secret(
    id = secretId,
    description = description,
    createdTimestamp = createdTimestamp,
    lastUsedTimestamp = lastUsedTimestamp,
) to secretToken
