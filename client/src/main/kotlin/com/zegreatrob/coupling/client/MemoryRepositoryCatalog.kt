package com.zegreatrob.coupling.client

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.user.UserIdSyntax
import com.zegreatrob.coupling.repository.memory.MemoryPairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.memory.MemoryPinRepository
import com.zegreatrob.coupling.repository.memory.MemoryPlayerRepository
import com.zegreatrob.coupling.repository.memory.MemoryTribeRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.sdk.RepositoryCatalog

class MemoryRepositoryCatalog private constructor(
    override val userId: String,
    override val clock: TimeProvider,
    override val tribeRepository: TribeRepository,
    override val playerRepository: PlayerEmailRepository,
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository,
    override val pinRepository: PinRepository
) :
    RepositoryCatalog,
    UserIdSyntax,
    ClockSyntax {

    constructor(userEmail: String, backend: LocalStorageRepositoryBackend, clock: TimeProvider) : this(
        userEmail,
        clock,
        MemoryTribeRepository(userEmail, clock, backend.tribe),
        MemoryPlayerRepository(userEmail, clock, backend.player),
        MemoryPairAssignmentDocumentRepository(userEmail, clock, backend.pairAssignments),
        MemoryPinRepository(userEmail, clock, backend.pin)
    )

}
