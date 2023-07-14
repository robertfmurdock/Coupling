package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.sdk.gql.KtorQueryPerformer
import com.zegreatrob.coupling.sdk.gql.QueryPerformer
import com.zegreatrob.testmints.action.ActionCannon
import io.ktor.client.HttpClient

fun couplingSdk(getIdTokenFunc: suspend () -> String, httpClient: HttpClient) = ActionCannon<CouplingSdkDispatcher>(
    KtorCouplingSdkDispatcher(getIdTokenFunc, httpClient),
)

class KtorCouplingSdkDispatcher(
    val getIdTokenFunc: suspend () -> String,
    httpClient: HttpClient,
) : CouplingSdkDispatcher,
    SdkBoost,
    SdkCreateSecretCommandDispatcher,
    SdkDeletePairAssignmentsCommandDispatcher,
    SdkDeletePartyCommandDispatcher,
    SdkDeletePinCommandDispatcher,
    SdkDeletePlayerCommandDispatcher,
    SdkDeleteSecretCommandDispatcher,
    SdkGrantSlackAccess,
    SdkGraphQueryDispatcher,
    SdkSavePairAssignmentsCommandDispatcher,
    SdkSavePartyCommandDispatcher,
    SdkSavePinCommandDispatcher,
    SdkSavePlayerCommandDispatcher,
    SdkSaveSlackIntegrationCommandDispatcher,
    SdkSpin {
    override val performer: QueryPerformer = StandardPartyGQLPerformer(getIdTokenFunc, httpClient)
}

class StandardPartyGQLPerformer(private val getIdTokenFunc: suspend () -> String, httpClient: HttpClient) :
    KtorQueryPerformer {
    override val client = httpClient
    override suspend fun getIdToken() = getIdTokenFunc.invoke()
}

interface SdkProviderSyntax {
    val sdk: ActionCannon<CouplingSdkDispatcher>
}
