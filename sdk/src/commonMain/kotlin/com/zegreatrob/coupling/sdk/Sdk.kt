package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentDelete
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentGet
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentSave

interface RepositoryCatalog {
    val tribeRepository: TribeRepository
    val playerRepository: PlayerRepository
    val pinRepository: PinRepository
    val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}

interface SdkTribeRepository : SdkTribeGet, SdkTribeListGet,
    SdkTribeSave, SdkTribeDelete, TribeRepository

interface SdkPlayerRepository : SdkPlayerListGet,
    SdkPlayerGetDeleted, SdkPlayerSave,
    SdkPlayerDeleter, PlayerRepository

interface SdkPinRepository : SdkPinGet, SdkPinSave, SdkPinDelete, PinRepository

interface SdkPairAssignmentsRepository : SdkPairAssignmentDocumentGet,
    SdkPairAssignmentDocumentSave,
    SdkPairAssignmentDocumentDelete,
    SdkPairAssignmentDocumentGetCurrent,
    PairAssignmentDocumentRepository

interface Sdk : RepositoryCatalog, SdkBoostRepository, SdkSpin, SdkUserGet, SdkSyntax, GqlQueryComponent {
    override val sdk: Sdk get() = this
    override val pinRepository get() :SdkPinRepository = object : SdkPinRepository, GqlQueryComponent by this {}
    override val pairAssignmentDocumentRepository: SdkPairAssignmentsRepository
        get() = object : SdkPairAssignmentsRepository, GqlQueryComponent by this {}
    override val playerRepository
        get() : SdkPlayerRepository = object : SdkPlayerRepository, GqlQueryComponent by this {}
    override val tribeRepository get() : SdkTribeRepository = object : SdkTribeRepository, GqlQueryComponent by this {}
}

class SdkSingleton(getIdTokenFunc: suspend () -> String) : Sdk,
    TribeGQLPerformer by BatchingTribeGQLPerformer(object : KtorQueryPerformer {
        override suspend fun getIdToken(): String = getIdTokenFunc.invoke()
    })

interface SdkSyntax {
    val sdk: Sdk
}
