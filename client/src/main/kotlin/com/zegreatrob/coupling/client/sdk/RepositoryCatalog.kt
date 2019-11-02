package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.model.player.PlayerDeleter
import com.zegreatrob.coupling.model.player.PlayerGetDeleted
import com.zegreatrob.coupling.model.player.PlayerGetter
import com.zegreatrob.coupling.model.player.PlayerSaver

interface RepositoryCatalog {
    val tribeRepository: SdkTribeRepository
    val playerRepository: SdkPlayerRepository
    val pinRepository: SdkPinRepository
    val pairAssignmentDocumentRepository: SdkPairAssignmentsRepository
}

interface SdkTribeRepository : SdkGetTribe, SdkTribeListGet, SdkTribeSave
interface SdkPlayerRepository : SdkPlayerGetter, SdkPlayerGetDeleted, SdkPlayerSaver, SdkPlayerDeleter, PlayerRepository
interface SdkPinRepository : SdkPinGetter
interface SdkPairAssignmentsRepository : SdkPairAssignmentDocumentGetter, SdkPairAssignmentDocumentSaver

interface PlayerRepository : PlayerGetter, PlayerSaver, PlayerDeleter, PlayerGetDeleted

object SdkRepositoryCatalog : RepositoryCatalog, SdkTribeRepository, SdkPlayerRepository,
    SdkPairAssignmentsRepository, SdkPinRepository {
    override val pinRepository get() = this
    override val pairAssignmentDocumentRepository get() = this
    override val playerRepository get() = this
    override val tribeRepository get() = this
}
