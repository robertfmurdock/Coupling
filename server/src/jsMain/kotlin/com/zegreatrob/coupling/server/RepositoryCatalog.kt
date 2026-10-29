package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.LiveInfoRepository
import com.zegreatrob.coupling.repository.contribution.ContributionRepository
import com.zegreatrob.coupling.repository.discord.DiscordAccessRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoUserRepository
import com.zegreatrob.coupling.repository.dynamo.MigrationMode
import com.zegreatrob.coupling.repository.dynamo.TargetSchemaBootstrap
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
    dynamoRepositoryCatalog(user.id)
}

private suspend fun dynamoRepositoryCatalog(userId: UserId): RepositoryCatalog = when (ensureLegacyMigrationCatalogSelection()) {
    MigrationCatalogSelection.LEGACY -> CachedRepositoryCatalog(DynamoRepositoryCatalog(userId, Clock.System))
}

val memoryBackend by lazy { MemoryRepositoryBackend() }

private fun memoryRepositoryCatalog(userId: UserId) = MemoryRepositoryCatalog(userId, memoryBackend, Clock.System)

suspend fun userRepository(userId: UserId): UserRepository = if (useInMemory()) {
    memoryRepositoryCatalog(userId).userRepository
} else {
    ensureLegacyMigrationCatalogSelection()
    DynamoUserRepository(userId, Clock.System)
}

suspend fun secretRepository(userId: UserId): SecretRepository = if (useInMemory()) {
    memoryRepositoryCatalog(userId).secretRepository
} else {
    ensureLegacyMigrationCatalogSelection()
    DynamoSecretRepository(userId, Clock.System)
}

fun useInMemory() = Process.getEnv("COUPLING_IN_MEMORY") == "true"

private fun migrationMode() = MigrationMode.parse(Process.getEnv("DYNAMODB_SCHEMA_MIGRATION_MODE"))

private suspend fun ensureLegacyMigrationCatalogSelection() = migrationMode().let { requestedMode ->
    val stage = Process.getEnv("STAGE") ?: throw IllegalStateException("STAGE is required for DynamoDB schema migration")
    val ledgerPhase = when (requestedMode) {
        MigrationMode.DISABLED -> TargetSchemaBootstrap.existingState(stage)?.phase

        MigrationMode.DUAL_WRITE,
        MigrationMode.CUTOVER,
        -> TargetSchemaBootstrap.ensure(stage, requestedMode).phase
    }
    migrationCatalogSelection(requestedMode, ledgerPhase)
}
