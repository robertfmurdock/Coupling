package com.zegreatrob.coupling.sdk

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

class SdkPlayerRepository(gqlQueryComponent: GqlQueryComponent) :
    SdkPlayerListGet,
    SdkPlayerGetDeleted,
    SdkPlayerSave,
    SdkPlayerDeleter,
    PlayerRepository,
    GqlQueryComponent by gqlQueryComponent

class SdkPinRepository(gqlQueryComponent: GqlQueryComponent) :
    SdkPinGet,
    SdkPinSave,
    SdkPinDelete,
    PinRepository,
    GqlQueryComponent by gqlQueryComponent

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
    GqlQueryComponent,
    GqlFileLoader {
    suspend fun getToken(): String
    override val sdk: Sdk get() = this
    override val pinRepository get() = SdkPinRepository(this)
    override val pairAssignmentDocumentRepository get() = SdkPairAssignmentsRepository(this)
    override val playerRepository get() = SdkPlayerRepository(this)
    override val partyRepository get() = SdkPartyRepository(this)
    override val mutations get() = Mutations(this)
    override val queries get() = Queries(this)
}

class SdkSingleton(val getIdTokenFunc: suspend () -> String, val httpClient: HttpClient) :
    Sdk,
    TribeGQLPerformer by BatchingTribeGQLPerformer(StandardTribeGQLPerformer(getIdTokenFunc, httpClient)) {
    override suspend fun getToken(): String = getIdTokenFunc()
}

class StandardTribeGQLPerformer(val getIdTokenFunc: suspend () -> String, httpClient: HttpClient) : KtorQueryPerformer {
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
