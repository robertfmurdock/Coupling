package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.user.CreateConnectUserSecretCommand
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.CreateConnectUserSecretMutation
import com.zegreatrob.coupling.sdk.schema.CreateConnectUserSecretMutation.CreateConnectUserSecret

interface SdkCreateConnectUserSecretCommand :
    CreateConnectUserSecretCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: CreateConnectUserSecretCommand): Pair<Secret, String>? = CreateConnectUserSecretMutation().execute()
        .dataOrThrow()
        .createConnectUserSecret
        ?.toDomain()
}

internal fun CreateConnectUserSecret.toDomain(): Pair<Secret, String> = Secret(
    id = secretId,
    description = description,
    createdTimestamp = createdTimestamp,
    lastUsedTimestamp = lastUsedTimestamp,
) to secretToken
