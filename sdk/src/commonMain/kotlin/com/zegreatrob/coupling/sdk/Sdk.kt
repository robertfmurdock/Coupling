package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyPinsSyntax
import com.zegreatrob.coupling.repository.party.PartyIdDeleteSyntax
import com.zegreatrob.coupling.repository.party.PartyListSyntax
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.coupling.repository.party.PartySaveSyntax
import com.zegreatrob.coupling.repository.pin.PartyPinSaveSyntax
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PartyPlayerSaveSyntax
import com.zegreatrob.coupling.repository.player.PartyPlayersSyntax
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentDelete
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentGet
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentSave
import com.zegreatrob.coupling.sdk.user.SdkUserGet
import com.zegreatrob.coupling.sdk.user.SdkUserQueryDispatcher
import io.ktor.client.HttpClient

interface RepositoryCatalog {
    val partyRepository: PartyRepository
    val playerRepository: PlayerRepository
    val pinRepository: PinRepository
    val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}

class SdkPartyRepository(gqlQueryComponent: GqlQueryComponent) :
    SdkPartyGet,
    SdkPartyListGet,
    SdkPartySave,
    SdkPartyDelete,
    PartyRepository,
    GqlQueryComponent by gqlQueryComponent

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

class SdkPairAssignmentsRepository(gqlQueryComponent: GqlQueryComponent) :
    SdkPairAssignmentDocumentGet,
    SdkPairAssignmentDocumentSave,
    SdkPairAssignmentDocumentDelete,
    SdkPairAssignmentDocumentGetCurrent,
    PairAssignmentDocumentRepository,
    GqlQueryComponent by gqlQueryComponent

interface Sdk :
    BarebonesSdk,
    RepositoryCatalog,
    SdkBoostRepository,
    SdkSpin,
    SdkUserGet,
    SdkUserQueryDispatcher,
    SdkSyntax,
    SdkPlayer,
    SdkPin,
    ClientDeletePartyCommandDispatcher,
    ClientDeletePinCommandDispatcher,
    ClientDeletePlayerCommandDispatcher,
    ClientSavePartyCommandDispatcher,
    ClientSavePinCommandDispatcher,
    ClientSavePlayerCommandDispatcher,
    ClientDeletePairAssignmentsCommandDispatcher,
    ClientSavePairAssignmentsCommandDispatcher,
    GqlQueryComponent,
    GqlFileLoader {
    suspend fun getToken(): String
    override val sdk: Sdk get() = this
    override val pinRepository get() = this
    override val pairAssignmentDocumentRepository get() = SdkPairAssignmentsRepository(this)
    override val playerRepository get() = this
    override val partyRepository get() = SdkPartyRepository(this)
    override val mutations get() = Mutations(this)
    override val queries get() = Queries(this)
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
    PartyListSyntax,
    PartySaveSyntax,
    PartyIdDeleteSyntax,
    PartyPinsSyntax,
    PartyPinSaveSyntax,
    PartyPlayerSaveSyntax,
    PartyPlayersSyntax {
    override val partyRepository: PartyRepository
    override val pinRepository: PinRepository
    override val playerRepository: PlayerRepository
}
