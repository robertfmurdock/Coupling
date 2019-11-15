package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.player.PlayerDeleter
import com.zegreatrob.coupling.model.player.PlayerGetDeleted
import com.zegreatrob.coupling.model.player.PlayerGetter
import com.zegreatrob.coupling.model.player.PlayerSaver
import com.zegreatrob.coupling.model.tribe.TribeRepository
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentDeleter
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentGetter
import com.zegreatrob.coupling.sdk.pairassignments.SdkPairAssignmentDocumentSaver

interface RepositoryCatalog {
    val tribeRepository: SdkTribeRepository
    val playerRepository: SdkPlayerRepository
    val pinRepository: SdkPinRepository
    val pairAssignmentDocumentRepository: SdkPairAssignmentsRepository
}

interface SdkTribeRepository : SdkGetTribe, SdkTribeListGet,
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
    SdkLogout

object SdkSingleton : Sdk {
    override val sdk: Sdk get() = this
    override val pinRepository get() = this
    override val pairAssignmentDocumentRepository get() = this
    override val playerRepository get() = this
    override val tribeRepository get() = this
}

interface SdkSyntax {
    val sdk: Sdk
}
