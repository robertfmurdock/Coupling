package com.zegreatrob.coupling.server

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.dynamo.*
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.user.UserRepository

class DynamoRepositoryCatalog private constructor(
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
        suspend operator fun invoke(userEmail: String, clock: TimeProvider): DynamoRepositoryCatalog {
            val tribeRepository = DynamoTribeRepository(userEmail, clock)
            val playerRepository = DynamoPlayerRepository(userEmail, clock)
            val pairAssignmentDocumentRepository = DynamoPairAssignmentDocumentRepository(userEmail, clock)
            val pinRepository = DynamoPinRepository(userEmail, clock)
            val userRepository = DynamoUserRepository(userEmail, clock)
            return DynamoRepositoryCatalog(
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
