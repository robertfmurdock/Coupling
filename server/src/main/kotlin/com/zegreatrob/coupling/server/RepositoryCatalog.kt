package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.dynamo.DynamoUserRepository
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.user.UserRepository
import kotlinx.coroutines.CoroutineScope

interface RepositoryCatalog {
    val tribeRepository: TribeRepository
    val playerRepository: PlayerEmailRepository
    val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
    val pinRepository: PinRepository
    val userRepository: UserRepository
}

suspend fun commandDispatcher(user: User, scope: CoroutineScope, traceId: Uuid): ActionDispatcher {
    val repositoryCatalog = repositoryCatalog(user)
    return ActionDispatcher(user, repositoryCatalog, scope, traceId)
}

private suspend fun repositoryCatalog(user: User): RepositoryCatalog = if (useInMemory())
    memoryRepositoryCatalog(user.id)
else
    DynamoRepositoryCatalog(user.id, TimeProvider)

val memoryBackend by lazy { MemoryRepositoryBackend() }

private fun memoryRepositoryCatalog(userId: String) = MemoryRepositoryCatalog(
    userId,
    memoryBackend,
    TimeProvider
)

suspend fun userRepository(userId: String): UserRepository = if (useInMemory())
    memoryRepositoryCatalog(userId).userRepository
else
    DynamoUserRepository(userId, TimeProvider)

private fun useInMemory() = Process.getEnv("COUPLING_IN_MEMORY") == "true"
