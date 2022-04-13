package com.zegreatrob.coupling.server

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserIdSyntax
import com.zegreatrob.coupling.repository.LiveInfoRepository
import com.zegreatrob.coupling.repository.memory.*
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.user.UserRepository

class MemoryRepositoryCatalog private constructor(
    override val userId: String,
    override val clock: TimeProvider,
    override val tribeRepository: TribeRepository,
    override val playerRepository: PlayerEmailRepository,
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository,
    override val pinRepository: PinRepository,
    override val userRepository: UserRepository,
    override val liveInfoRepository: LiveInfoRepository
) :
    RepositoryCatalog,
    UserIdSyntax,
    ClockSyntax {

    init {
        println("initializing MemoryRepositoryCatalog")
    }

    companion object {
        operator fun invoke(userEmail: String, backend: MemoryRepositoryBackend, clock: TimeProvider) =
            MemoryRepositoryCatalog(
                userEmail,
                clock,
                MemoryTribeRepository(userEmail, clock, backend.tribe),
                MemoryPlayerRepository(userEmail, clock, backend.player),
                MemoryPairAssignmentDocumentRepository(userEmail, clock, backend.pairAssignments),
                MemoryPinRepository(userEmail, clock, backend.pin),
                MemoryUserRepository(userEmail, clock, backend.user),
                MemoryLiveInfoRepository()
            )
    }
}

class MemoryRepositoryBackend {
    val tribe = SimpleRecordBackend<Party>()
    val player = SimpleRecordBackend<PartyElement<Player>>()
    val pairAssignments = SimpleRecordBackend<PartyElement<PairAssignmentDocument>>()
    val pin = SimpleRecordBackend<PartyElement<Pin>>()
    val user = SimpleRecordBackend<User>()
}
