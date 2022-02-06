package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPinsSyntax
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.pin.TribeIdPinSaveSyntax
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.repository.player.TribeIdPlayerSaveSyntax
import com.zegreatrob.coupling.repository.player.TribeIdPlayersSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdDeleteSyntax
import com.zegreatrob.coupling.repository.tribe.TribeListSyntax
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.tribe.TribeSaveSyntax
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentDelete
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentGet
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentSave
import io.ktor.client.*

interface RepositoryCatalog {
    val tribeRepository: TribeRepository
    val playerRepository: PlayerRepository
    val pinRepository: PinRepository
    val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}

class SdkTribeRepository(gqlQueryComponent: GqlQueryComponent) : SdkTribeGet, SdkTribeListGet,
    SdkTribeSave, SdkTribeDelete, TribeRepository,
    GqlQueryComponent by gqlQueryComponent

class SdkPlayerRepository(gqlQueryComponent: GqlQueryComponent) : SdkPlayerListGet,
    SdkPlayerGetDeleted, SdkPlayerSave, SdkPlayerDeleter, PlayerRepository,
    GqlQueryComponent by gqlQueryComponent

class SdkPinRepository(gqlQueryComponent: GqlQueryComponent) : SdkPinGet, SdkPinSave, SdkPinDelete, PinRepository,
    GqlQueryComponent by gqlQueryComponent

class SdkPairAssignmentsRepository(gqlQueryComponent: GqlQueryComponent) : SdkPairAssignmentDocumentGet,
    SdkPairAssignmentDocumentSave,
    SdkPairAssignmentDocumentDelete,
    SdkPairAssignmentDocumentGetCurrent,
    PairAssignmentDocumentRepository,
    GqlQueryComponent by gqlQueryComponent

interface Sdk : RepositoryCatalog, SdkBoostRepository, SdkSpin, SdkUserGet, SdkSyntax, GqlQueryComponent,
    GqlFileLoader {
    suspend fun getToken(): String
    override val sdk: Sdk get() = this
    override val pinRepository get() = SdkPinRepository(this)
    override val pairAssignmentDocumentRepository get() = SdkPairAssignmentsRepository(this)
    override val playerRepository get() = SdkPlayerRepository(this)
    override val tribeRepository get() = SdkTribeRepository(this)
    override val mutations get() = Mutations(this)
    override val queries get() = Queries(this)
}

class SdkSingleton(val getIdTokenFunc: suspend () -> String, val httpClient: HttpClient) : Sdk,
    TribeGQLPerformer by BatchingTribeGQLPerformer(StandardTribeGQLPerformer(getIdTokenFunc, httpClient)) {
    override suspend fun getToken(): String = getIdTokenFunc()
}

class StandardTribeGQLPerformer(val getIdTokenFunc: suspend () -> String, httpClient: HttpClient) : KtorQueryPerformer {
    override val client = httpClient
    override suspend fun getIdToken() = getIdTokenFunc.invoke()
}

interface SdkProviderSyntax {
    val sdk: Sdk
}

interface SdkSyntax: SdkProviderSyntax, TribeListSyntax, TribeSaveSyntax, TribeIdDeleteSyntax, TribeIdPinsSyntax, TribeIdPinSaveSyntax, TribeIdPlayerSaveSyntax, TribeIdPlayersSyntax {
    override val tribeRepository: TribeRepository
    override val pinRepository: PinRepository
    override val playerRepository: PlayerRepository
}
