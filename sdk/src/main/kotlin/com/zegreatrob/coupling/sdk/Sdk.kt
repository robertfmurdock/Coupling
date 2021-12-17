package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.sdk.external.axios.axios
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

interface Sdk : RepositoryCatalog, SdkTribeRepository, SdkPlayerRepository, SdkPairAssignmentsRepository,
    SdkPinRepository, SdkSpin, SdkSyntax, SdkLogout {
    override val sdk: Sdk get() = this
    override val pinRepository get() = this
    override val pairAssignmentDocumentRepository get() = this
    override val playerRepository get() = this
    override val tribeRepository get() = this
}

object SdkSingleton : Sdk, TribeGQLPerformer by BatchingTribeGQLPerformer(axios)

interface SdkSyntax {
    val sdk: Sdk
}
