package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.repository.tribe.TribeRepository
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
    override val sdk: Sdk get() = this
    override val pinRepository get() = SdkPinRepository(this)
    override val pairAssignmentDocumentRepository get() = SdkPairAssignmentsRepository(this)
    override val playerRepository get() = SdkPlayerRepository(this)
    override val tribeRepository get() = SdkTribeRepository(this)
    override val mutations get() = Mutations(this)
    override val queries get() = Queries(this)
}

class SdkSingleton(getIdTokenFunc: suspend () -> String, locationAndBasename: (Pair<String, String>)?) : Sdk,
    TribeGQLPerformer by BatchingTribeGQLPerformer(object : KtorQueryPerformer {
        override val client: HttpClient = defaultClient(locationAndBasename)
        override suspend fun getIdToken(): String = getIdTokenFunc.invoke()
    })

interface SdkSyntax {
    val sdk: Sdk
}
