package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.CouplingMutationResult

fun GqlMutation.toDomain() = CouplingMutationResult(
    createSecret = createSecret?.toDomain(),
    createConnectUserSecret = createConnectUserSecret?.toDomain(),
    deleteSecret = deleteSecret,
    saveSlackIntegration = saveSlackIntegration,
)
