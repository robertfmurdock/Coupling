package com.zegreatrob.coupling.server

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserEmailSyntax
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
    override val userRepository: UserRepository
) :
    RepositoryCatalog,
    UserEmailSyntax,
    ClockSyntax {

    companion object {
        suspend operator fun invoke(
            userEmail: String,
            backend: MemoryRepositoryBackend,
            clock: TimeProvider
        ): MemoryRepositoryCatalog {
            val tribeRepository = MemoryTribeRepository(userEmail, clock, backend.tribe)
            val playerRepository = MemoryPlayerRepository(userEmail, clock, backend.player)
            val pairAssignmentDocumentRepository =
                MemoryPairAssignmentDocumentRepository(userEmail, clock, backend.pairAssignments)
            val pinRepository = MemoryPinRepository(userEmail, clock, backend.pin)
            val userRepository = MemoryUserRepository(userEmail, clock, backend.user)
            return MemoryRepositoryCatalog(
                userEmail,
                clock,
                tribeRepository,
                playerRepository,
                pairAssignmentDocumentRepository,
                pinRepository,
                userRepository
            )
        }
    }

}

class MemoryRepositoryBackend {
    val tribe = SimpleRecordBackend<Tribe>()
    val player = SimpleRecordBackend<TribeIdPlayer>()
    val pairAssignments = SimpleRecordBackend<TribeIdPairAssignmentDocument>()
    val pin = SimpleRecordBackend<TribeIdPin>()
    val user = SimpleRecordBackend<User>()
}