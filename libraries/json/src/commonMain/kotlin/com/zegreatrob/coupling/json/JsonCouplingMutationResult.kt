package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.CouplingMutationResult
import com.zegreatrob.coupling.model.party.Secret
import kotlinx.serialization.Serializable

@Serializable
data class JsonCouplingMutationResult(
    val createSecret: JsonSecretToken? = null,
)

fun JsonCouplingMutationResult.toDomain() = CouplingMutationResult(
    createSecret = createSecret?.toDomain(),
)

fun JsonSecretToken.toDomain() = Secret(secretId) to secretToken
