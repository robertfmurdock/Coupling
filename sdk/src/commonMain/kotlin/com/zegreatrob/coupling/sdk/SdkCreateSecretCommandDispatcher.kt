package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CreateSecretCommand
import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.json.CreateSecretInput
import com.zegreatrob.coupling.json.JsonCouplingMutationResult
import com.zegreatrob.coupling.json.toDomain
import com.zegreatrob.coupling.sdk.gql.GqlSyntax
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

interface SdkCreateSecretCommandDispatcher : CreateSecretCommand.Dispatcher, GqlSyntax {

    override suspend fun perform(command: CreateSecretCommand) =
        doQuery(Mutation.createSecret, createSecretInput(command))
            .parseMutationResult()
            .toDomain()
            .createSecret
            ?.successResult()
            ?: NotFoundResult("secret")

    private fun createSecretInput(command: CreateSecretCommand) = CreateSecretInput(command.partyId.value)

    private fun JsonElement.parseMutationResult() = Json.decodeFromJsonElement<JsonCouplingMutationResult>(
        jsonObject["data"]!!.jsonObject,
    )
}
