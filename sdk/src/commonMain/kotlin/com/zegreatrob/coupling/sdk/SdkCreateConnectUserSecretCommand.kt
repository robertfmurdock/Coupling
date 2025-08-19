package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.user.CreateConnectUserSecretCommand
import com.zegreatrob.coupling.json.toDomain
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

interface SdkCreateConnectUserSecretCommand :
    CreateConnectUserSecretCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: CreateConnectUserSecretCommand): Pair<Secret, String>? = performQuery(
        body = JsonObject(mapOf("query" to JsonPrimitive(Mutation.createConnectUserSecret))),
    )
        .parseMutationResult()
        .toDomain()
        .createConnectUserSecret
}
