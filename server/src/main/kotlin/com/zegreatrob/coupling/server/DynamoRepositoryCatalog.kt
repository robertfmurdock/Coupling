package com.zegreatrob.coupling.server

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.dynamo.*
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.LiveInfoRepository
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
    override val userRepository: UserRepository,
    override val liveInfoRepository: LiveInfoRepository
) :
    RepositoryCatalog,
    UserEmailSyntax,
    ClockSyntax {
    companion object {
        suspend operator fun invoke(userId: String, clock: TimeProvider) = DynamoRepositoryCatalog(
            userId,
            clock,
            DynamoTribeRepository(userId, clock),
            DynamoPlayerRepository(userId, clock),
            DynamoPairAssignmentDocumentRepository(userId, clock),
            DynamoPinRepository(userId, clock),
            DynamoUserRepository(userId, clock),
            DynamoLiveInfoRepository(userId, clock)
        )
    }
}
