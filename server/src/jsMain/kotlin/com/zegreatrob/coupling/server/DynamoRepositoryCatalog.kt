package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.ClockProvider
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.LiveInfoRepository
import com.zegreatrob.coupling.repository.contribution.ContributionRepository
import com.zegreatrob.coupling.repository.discord.DiscordAccessRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoContributionRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoDiscordRepository
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
import com.zegreatrob.coupling.repository.slack.SlackAccessRepository
import com.zegreatrob.coupling.repository.user.UserRepository
import kotlin.time.Clock

class DynamoRepositoryCatalog private constructor(
    override val userId: UserId,
    override val clock: Clock,
    override val partyRepository: PartyRepository,
    override val playerRepository: PlayerEmailRepository,
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository,
    override val pinRepository: PinRepository,
    override val userRepository: UserRepository,
    override val liveInfoRepository: LiveInfoRepository,
    override val secretRepository: SecretRepository,
    override val slackAccessRepository: SlackAccessRepository,
    override val discordAccessRepository: DiscordAccessRepository,
    override val contributionRepository: ContributionRepository,
) : RepositoryCatalog,
    UserIdProvider,
    ClockProvider {
    companion object {
        suspend operator fun invoke(userId: UserId, clock: Clock) = DynamoRepositoryCatalog(
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
            DynamoDiscordRepository(userId, clock),
            DynamoContributionRepository(userId, clock),
        )
    }
}
