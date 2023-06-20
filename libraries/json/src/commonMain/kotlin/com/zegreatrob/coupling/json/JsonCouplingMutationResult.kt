package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.CouplingMutationResult
import com.zegreatrob.coupling.model.party.Secret
import kotlinx.serialization.Serializable

@Serializable
data class JsonCouplingMutationResult(
    val createSecret: JsonSecretToken? = null,
    val deleteSecret: Boolean? = null,
    val saveSlackIntegration: Boolean? = null,
)

fun JsonCouplingMutationResult.toDomain() = CouplingMutationResult(
    createSecret = createSecret?.toDomain(),
    deleteSecret = deleteSecret,
    saveSlackIntegration = saveSlackIntegration,
)

fun JsonSecretToken.toDomain(): Pair<Secret, String> = Secret(secretId) to secretToken
fun Pair<Secret, String>.toModel() = JsonSecretToken(first.id, second)
