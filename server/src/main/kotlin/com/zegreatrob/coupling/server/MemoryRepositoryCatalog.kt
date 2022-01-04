package com.zegreatrob.coupling.server

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserEmailSyntax
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
    UserEmailSyntax,
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
    val tribe = SimpleRecordBackend<Tribe>()
    val player = SimpleRecordBackend<TribeIdPlayer>()
    val pairAssignments = SimpleRecordBackend<TribeIdPairAssignmentDocument>()
    val pin = SimpleRecordBackend<TribeIdPin>()
    val user = SimpleRecordBackend<User>()
}
