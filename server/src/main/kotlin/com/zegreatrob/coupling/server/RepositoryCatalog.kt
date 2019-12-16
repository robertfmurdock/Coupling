package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.model.player.PlayerRepository
import com.zegreatrob.coupling.model.tribe.TribeRepository
import com.zegreatrob.coupling.model.user.UserRepository

interface RepositoryCatalog {
    val tribeRepository: TribeRepository
    val playerRepository: PlayerRepository
    val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
    val pinRepository: PinRepository
    val userRepository: UserRepository
}