package com.zegreatrob.coupling.client

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserIdSyntax
import com.zegreatrob.coupling.repository.memory.MemoryPairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.memory.MemoryPinRepository
import com.zegreatrob.coupling.repository.memory.MemoryPlayerRepository
import com.zegreatrob.coupling.repository.memory.MemoryTribeRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.sdk.BarebonesSdk

class MemoryRepositoryCatalog private constructor(
    override val userId: String,
    override val clock: TimeProvider,
    override val tribeRepository: TribeRepository,
    override val playerRepository: PlayerEmailRepository,
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository,
    override val pinRepository: PinRepository
) :
    BarebonesSdk,
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

    override suspend fun perform(query: UserQuery) = User(userId, "???", setOf(TribeId("Kind of fake")))

    override suspend fun requestSpin(tribeId: TribeId, players: List<Player>, pins: List<Pin>): PairAssignmentDocument {
        TODO("Not yet implemented")
    }

}
