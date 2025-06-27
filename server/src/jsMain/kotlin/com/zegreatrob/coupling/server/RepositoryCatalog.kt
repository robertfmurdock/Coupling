package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.LiveInfoRepository
import com.zegreatrob.coupling.repository.contribution.ContributionRepository
import com.zegreatrob.coupling.repository.discord.DiscordAccessRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoUserRepository
import com.zegreatrob.coupling.repository.dynamo.secret.DynamoSecretRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.repository.secret.SecretRepository
import com.zegreatrob.coupling.repository.slack.SlackAccessRepository
import com.zegreatrob.coupling.repository.user.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Clock
import kotlin.uuid.Uuid

interface RepositoryCatalog {
    val partyRepository: PartyRepository
    val playerRepository: PlayerEmailRepository
    val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
    val pinRepository: PinRepository
    val userRepository: UserRepository
    val liveInfoRepository: LiveInfoRepository
    val secretRepository: SecretRepository
    val slackAccessRepository: SlackAccessRepository
    val discordAccessRepository: DiscordAccessRepository
    val contributionRepository: ContributionRepository
}

suspend fun commandDispatcher(user: UserDetails, scope: CoroutineScope, traceId: Uuid) = CommandDispatcher(user, repositoryCatalog(user), scope, traceId)

private suspend fun repositoryCatalog(user: UserDetails): RepositoryCatalog = if (useInMemory()) {
    memoryRepositoryCatalog(user.id)
} else {
    CachedRepositoryCatalog(DynamoRepositoryCatalog(user.id, Clock.System))
}

val memoryBackend by lazy { MemoryRepositoryBackend() }

private fun memoryRepositoryCatalog(userId: UserId) = MemoryRepositoryCatalog(userId, memoryBackend, Clock.System)

suspend fun userRepository(userId: UserId): UserRepository = if (useInMemory()) {
    memoryRepositoryCatalog(userId).userRepository
} else {
    DynamoUserRepository(userId, Clock.System)
}

suspend fun secretRepository(userId: UserId): SecretRepository = if (useInMemory()) {
    memoryRepositoryCatalog(userId).secretRepository
} else {
    DynamoSecretRepository(userId, Clock.System)
}

fun useInMemory() = Process.getEnv("COUPLING_IN_MEMORY") == "true"
