package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.ClockProvider
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.LiveInfoRepository
import com.zegreatrob.coupling.repository.contribution.ContributionRepository
import com.zegreatrob.coupling.repository.discord.DiscordAccessRepository
import com.zegreatrob.coupling.repository.memory.MemoryContributionRepository
import com.zegreatrob.coupling.repository.memory.MemoryLiveInfoRepository
import com.zegreatrob.coupling.repository.memory.MemoryPairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.memory.MemoryPartyRepository
import com.zegreatrob.coupling.repository.memory.MemoryPinRepository
import com.zegreatrob.coupling.repository.memory.MemoryPlayerRepository
import com.zegreatrob.coupling.repository.memory.MemorySecretRepository
import com.zegreatrob.coupling.repository.memory.MemorySlackRepository
import com.zegreatrob.coupling.repository.memory.MemoryUserRepository
import com.zegreatrob.coupling.repository.memory.SimpleRecordBackend
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.repository.secret.SecretRepository
import com.zegreatrob.coupling.repository.slack.SlackAccessRepository
import com.zegreatrob.coupling.repository.user.UserRepository
import kotlinx.datetime.Clock

class MemoryRepositoryCatalog private constructor(
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
        operator fun invoke(userEmail: UserId, backend: MemoryRepositoryBackend, clock: Clock) = MemoryRepositoryCatalog(
            userEmail,
            clock,
            MemoryPartyRepository(userEmail, clock, backend.party),
            MemoryPlayerRepository(userEmail, clock, backend.player),
            MemoryPairAssignmentDocumentRepository(userEmail, clock, backend.pairAssignments),
            MemoryPinRepository(userEmail, clock, backend.pin),
            MemoryUserRepository(userEmail, clock, backend.user),
            MemoryLiveInfoRepository(),
            MemorySecretRepository(),
            MemorySlackRepository(),
            MemoryDiscordRepository(),
            MemoryContributionRepository(),
        )
    }
}

class MemoryRepositoryBackend {
    val party = SimpleRecordBackend<PartyDetails>()
    val player = SimpleRecordBackend<PartyElement<Player>>()
    val pairAssignments = SimpleRecordBackend<PartyElement<PairAssignmentDocument>>()
    val pin = SimpleRecordBackend<PartyElement<Pin>>()
    val user = SimpleRecordBackend<UserDetails>()
}
