package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.repository.player.PlayerDeleter
import com.zegreatrob.coupling.repository.player.PlayerGetDeleted
import com.zegreatrob.coupling.repository.player.PlayerGetter
import com.zegreatrob.coupling.repository.player.PlayerSaver
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.sdk.external.axios.axios
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentDeleter
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentGetter
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentSaver

interface RepositoryCatalog {
    val tribeRepository: SdkTribeRepository
    val playerRepository: SdkPlayerRepository
    val pinRepository: SdkPinRepository
    val pairAssignmentDocumentRepository: SdkPairAssignmentsRepository
}

interface SdkTribeRepository : SdkTribeGet, SdkTribeListGet,
    SdkTribeSave, SdkTribeDelete, TribeRepository

interface SdkPlayerRepository : SdkPlayerGetter,
    SdkPlayerGetDeleted, SdkPlayerSaver,
    SdkPlayerDeleter, PlayerRepository

interface SdkPinRepository : SdkPinGetter, SdkPinSaver, SdkPinDeleter

interface SdkPairAssignmentsRepository : SdkPairAssignmentDocumentGetter,
    SdkPairAssignmentDocumentSaver,
    SdkPairAssignmentDocumentDeleter

interface PlayerRepository : PlayerGetter, PlayerSaver, PlayerDeleter, PlayerGetDeleted

interface Sdk : RepositoryCatalog, SdkTribeRepository,
    SdkPlayerRepository, SdkPairAssignmentsRepository,
    SdkPinRepository, SdkSpin,
    SdkSyntax, SdkCreateGoogleSession,
    SdkLogout {
    override val sdk: Sdk get() = this
    override val pinRepository get() = this
    override val pairAssignmentDocumentRepository get() = this
    override val playerRepository get() = this
    override val tribeRepository get() = this
}

object SdkSingleton : Sdk, TribeGQLSyntax by BatchingTribeGQLSyntax(axios)

interface SdkSyntax {
    val sdk: Sdk
}
