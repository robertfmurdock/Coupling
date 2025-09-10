package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.secret.CreateSecretCommand
import com.zegreatrob.coupling.json.GqlMutation
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.CreateSecretMutation
import com.zegreatrob.coupling.sdk.schema.type.CreateSecretInput
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

interface SdkCreateSecretCommandDispatcher :
    CreateSecretCommand.Dispatcher,
    GqlTrait {

    override suspend fun perform(command: CreateSecretCommand) = CreateSecretMutation(createSecretInput(command)).execute()
        .data
        ?.createSecret
        ?.toDomain()

    private fun createSecretInput(command: CreateSecretCommand) = CreateSecretInput(
        partyId = command.partyId,
        description = command.description,
    )
}

fun JsonElement.parseMutationResult() = couplingJsonFormat.decodeFromJsonElement<GqlMutation>(
    jsonObject["data"]!!.jsonObject,
)
internal fun CreateSecretMutation.CreateSecret.toDomain(): Pair<Secret, String>? = Secret(
    id = secretId,
    description = description,
    createdTimestamp = createdTimestamp,
    lastUsedTimestamp = lastUsedTimestamp,
) to secretToken
