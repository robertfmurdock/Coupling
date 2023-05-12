package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyPinsSyntax
import com.zegreatrob.coupling.repository.party.PartySaveSyntax
import com.zegreatrob.coupling.repository.pin.PartyPinSaveSyntax
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PartyPlayerSaveSyntax
import com.zegreatrob.coupling.repository.player.PartyPlayersSyntax
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.sdk.pairassignments.SdkDeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentDelete
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentGet
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentSave
import com.zegreatrob.coupling.sdk.pairassignments.SdkSavePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.sdk.user.SdkUserQueryDispatcher
import io.ktor.client.HttpClient

interface SdkParty :
    SdkPartyGet,
    SdkPartySave,
    SdkPartyDelete,
    GqlQueryComponent

interface SdkPlayer :
    SdkPlayerListGet,
    SdkPlayerGetDeleted,
    SdkPlayerSave,
    SdkPlayerDeleter,
    PlayerRepository,
    GqlQueryComponent

interface SdkPin :
    SdkPinGet,
    SdkPinSave,
    SdkPinDelete,
    PinRepository,
    GqlQueryComponent

interface SdkPairAssignments :
    SdkPairAssignmentDocumentGet,
    SdkPairAssignmentDocumentSave,
    SdkPairAssignmentDocumentDelete,
    SdkPairAssignmentDocumentGetCurrent,
    PairAssignmentDocumentRepository,
    GqlQueryComponent

interface Sdk :
    BarebonesSdk,
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
    GqlQueryComponent,
    SdkBoost,
    SdkDeletePairAssignmentsCommandDispatcher,
    SdkDeletePartyCommandDispatcher,
    SdkDeletePinCommandDispatcher,
    SdkDeletePlayerCommandDispatcher,
    SdkPairAssignments,
    SdkParty,
    SdkPin,
    SdkPlayer,
    SdkSavePairAssignmentsCommandDispatcher,
    SdkSavePartyCommandDispatcher,
    SdkSavePinCommandDispatcher,
    SdkSavePlayerCommandDispatcher,
    SdkSpin,
    SdkSyntax,
    SdkUserQueryDispatcher,
    SdkGraphQueryDispatcher {
    suspend fun getToken(): String
    override val sdk: Sdk get() = this
    override val pinRepository get() = this
    override val pairAssignmentDocumentRepository get() = this
    override val playerRepository get() = this
    override val partyRepository get() = this
    override val mutations get() = Mutation
    override val queries get() = Query
}

class SdkSingleton(
    val getIdTokenFunc: suspend () -> String,
    private val httpClient: HttpClient,
    override val traceId: Uuid,
) : Sdk,
    PartyGQLPerformer by BatchingPartyGQLPerformer(StandardPartyGQLPerformer(getIdTokenFunc, httpClient)) {
    override suspend fun getToken(): String = getIdTokenFunc()
}

class StandardPartyGQLPerformer(private val getIdTokenFunc: suspend () -> String, httpClient: HttpClient) :
    KtorQueryPerformer {
    override val client = httpClient
    override suspend fun getIdToken() = getIdTokenFunc.invoke()
}

interface SdkProviderSyntax {
    val sdk: BarebonesSdk
}

interface SdkSyntax :
    SdkProviderSyntax,
    PartySaveSyntax,
    PartyPinsSyntax,
    PartyPinSaveSyntax,
    PartyPlayerSaveSyntax,
    PartyPlayersSyntax {
    override val pinRepository: PinRepository
    override val playerRepository: PlayerRepository
}
