package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.sdk.pairassignments.SdkDeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentSave
import com.zegreatrob.coupling.sdk.pairassignments.SdkSavePairAssignmentsCommandDispatcher
import io.ktor.client.HttpClient

interface Sdk :
    SdkApi,
    ClientHistoryQueryDispatcher,
    ClientNewPairAssignmentsCommandDispatcher,
    ClientPartyCurrentDataQueryDispatcher,
    ClientPartyPinListQueryDispatcher,
    ClientPartyPinQueryDispatcher,
    ClientPartyPlayerQueryDispatcher,
    ClientPartyQueryDispatcher,
    ClientRetiredPlayerListQueryDispatcher,
    ClientRetiredPlayerQueryDispatcher,
    ClientStatisticsQueryDispatcher,
    SdkBoost,
    SdkDeletePairAssignmentsCommandDispatcher,
    SdkDeletePartyCommandDispatcher,
    SdkDeletePinCommandDispatcher,
    SdkDeletePlayerCommandDispatcher,
    SdkGraphQueryDispatcher,
    SdkPairAssignmentDocumentSave,
    SdkSavePairAssignmentsCommandDispatcher,
    SdkSavePartyCommandDispatcher,
    SdkSavePinCommandDispatcher,
    SdkSavePlayerCommandDispatcher,
    SdkSpin,
    SdkProviderSyntax {
    suspend fun getToken(): String
    override val sdk: Sdk get() = this
}

class SdkSingleton(
    val getIdTokenFunc: suspend () -> String,
    override val traceId: Uuid,
    httpClient: HttpClient,
) : Sdk {
    override suspend fun getToken(): String = getIdTokenFunc()
    override val performer: QueryPerformer = StandardPartyGQLPerformer(getIdTokenFunc, httpClient)
}

class StandardPartyGQLPerformer(private val getIdTokenFunc: suspend () -> String, httpClient: HttpClient) :
    KtorQueryPerformer {
    override val client = httpClient
    override suspend fun getIdToken() = getIdTokenFunc.invoke()
}

interface SdkProviderSyntax {
    val sdk: SdkApi
}
