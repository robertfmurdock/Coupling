package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.user.UserIdSyntax
import com.zegreatrob.coupling.repository.LiveInfoRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoLiveInfoRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoPairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoPartyRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoPinRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoPlayerRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoUserRepository
import com.zegreatrob.coupling.repository.dynamo.secret.DynamoSecretRepository
import com.zegreatrob.coupling.repository.dynamo.slack.DynamoSlackRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.repository.secret.SecretRepository
import com.zegreatrob.coupling.repository.slack.SlackSave
import com.zegreatrob.coupling.repository.user.UserRepository
import korlibs.time.TimeProvider

class DynamoRepositoryCatalog private constructor(
    override val userId: String,
    override val clock: TimeProvider,
    override val partyRepository: PartyRepository,
    override val playerRepository: PlayerEmailRepository,
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository,
    override val pinRepository: PinRepository,
    override val userRepository: UserRepository,
    override val liveInfoRepository: LiveInfoRepository,
    override val secretRepository: SecretRepository,
    override val slackRepository: SlackSave,
) :
    RepositoryCatalog,
    UserIdSyntax,
    ClockSyntax {
    companion object {
        suspend operator fun invoke(userId: String, clock: TimeProvider) = DynamoRepositoryCatalog(
            userId,
            clock,
            DynamoPartyRepository(userId, clock),
            DynamoPlayerRepository(userId, clock),
            DynamoPairAssignmentDocumentRepository(userId, clock),
            DynamoPinRepository(userId, clock),
            DynamoUserRepository(userId, clock),
            DynamoLiveInfoRepository(userId, clock),
            DynamoSecretRepository(userId, clock),
            DynamoSlackRepository(userId, clock),
        )
    }
}
