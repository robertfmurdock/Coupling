package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.secret.CreateSecretCommand
import com.zegreatrob.coupling.json.GqlCreateSecretInput
import com.zegreatrob.coupling.json.GqlMutation
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toDomain
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

interface SdkCreateSecretCommandDispatcher :
    CreateSecretCommand.Dispatcher,
    GqlTrait {

    override suspend fun perform(command: CreateSecretCommand) =
        doQuery(Mutation.createSecret, createSecretInput(command))
            .parseMutationResult()
            .toDomain()
            .createSecret

    private fun createSecretInput(command: CreateSecretCommand) = GqlCreateSecretInput(
        partyId = command.partyId.value,
        description = command.description,
    )
}

fun JsonElement.parseMutationResult() = couplingJsonFormat.decodeFromJsonElement<GqlMutation>(
    jsonObject["data"]!!.jsonObject,
)
