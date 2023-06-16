package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.LoggingActionExecuteSyntax
import com.zegreatrob.coupling.sdk.gql.KtorQueryPerformer
import com.zegreatrob.coupling.sdk.gql.QueryPerformer
import io.ktor.client.HttpClient

class KtorCouplingSdk(
    val getIdTokenFunc: suspend () -> String,
    override val traceId: Uuid,
    httpClient: HttpClient,
) : CouplingSdk,
    LoggingActionExecuteSyntax,
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
    SdkSpin {
    override val performer: QueryPerformer = StandardPartyGQLPerformer(getIdTokenFunc, httpClient)
}

class StandardPartyGQLPerformer(private val getIdTokenFunc: suspend () -> String, httpClient: HttpClient) :
    KtorQueryPerformer {
    override val client = httpClient
    override suspend fun getIdToken() = getIdTokenFunc.invoke()
}

interface SdkProviderSyntax {
    val sdk: CouplingSdk
}
