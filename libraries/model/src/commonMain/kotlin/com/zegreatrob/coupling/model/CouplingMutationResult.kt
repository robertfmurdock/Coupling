package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.party.Secret

data class CouplingMutationResult(
    val createSecret: Pair<Secret, String>? = null,
    val createConnectUserSecret: Pair<Secret, String>? = null,
    val deleteSecret: Boolean? = null,
    val saveSlackIntegration: Boolean?,
)
