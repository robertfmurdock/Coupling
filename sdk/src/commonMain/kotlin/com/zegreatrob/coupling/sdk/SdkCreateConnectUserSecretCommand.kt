package com.zegreatrob.coupling.sdk

import com.example.CreateConnectUserSecretMutation
import com.example.CreateConnectUserSecretMutation.CreateConnectUserSecret
import com.zegreatrob.coupling.action.user.CreateConnectUserSecretCommand
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkCreateConnectUserSecretCommand :
    CreateConnectUserSecretCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: CreateConnectUserSecretCommand): Pair<Secret, String>? = apolloMutation(
        CreateConnectUserSecretMutation(),
    )
        .dataOrThrow()
        .createConnectUserSecret
        ?.toDomain()
}

fun CreateConnectUserSecret.toDomain(): Pair<Secret, String>? = Secret(
    id = secretId,
    description = description,
    createdTimestamp = createdTimestamp,
    lastUsedTimestamp = lastUsedTimestamp,
) to secretToken
