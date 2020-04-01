package com.zegreatrob.coupling.server

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.memory.*
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.user.UserRepository

class MemoryRepositoryCatalog private constructor(
    override val userEmail: String,
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
        suspend operator fun invoke(userEmail: String, clock: TimeProvider): MemoryRepositoryCatalog {
            val tribeRepository =
                MemoryTribeRepository(userEmail, clock)
            val playerRepository =
                MemoryPlayerRepository(userEmail, clock)
            val pairAssignmentDocumentRepository =
                MemoryPairAssignmentDocumentRepository(
                    userEmail,
                    clock
                )
            val pinRepository =
                MemoryPinRepository(userEmail, clock)
            val userRepository =
                MemoryUserRepository(userEmail, clock)
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