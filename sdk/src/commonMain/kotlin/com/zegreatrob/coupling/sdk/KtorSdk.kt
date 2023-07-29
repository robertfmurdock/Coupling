package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.sdk.gql.KtorQueryPerformer
import com.zegreatrob.coupling.sdk.gql.QueryPerformer
import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.testmints.action.ActionPipe
import com.zegreatrob.testmints.action.DispatcherPipeCannon
import io.ktor.client.HttpClient

fun couplingSdk(
    getIdTokenFunc: suspend () -> String,
    httpClient: HttpClient,
    pipe: ActionPipe = ActionPipe,
) = DispatcherPipeCannon<CouplingSdkDispatcher>(
    KtorCouplingSdkDispatcher(getIdTokenFunc, httpClient),
    pipe = pipe,
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
    SdkGrantDiscordAccess,
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
