package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.LiveInfoRepository
import com.zegreatrob.coupling.repository.dynamo.DynamoUserRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.repository.user.UserRepository
import kotlinx.coroutines.CoroutineScope

interface RepositoryCatalog {
    val partyRepository: PartyRepository
    val playerRepository: PlayerEmailRepository
    val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
    val pinRepository: PinRepository
    val userRepository: UserRepository
    val liveInfoRepository: LiveInfoRepository
}

suspend fun commandDispatcher(user: User, scope: CoroutineScope, traceId: Uuid) =
    CommandDispatcher(user, repositoryCatalog(user), scope, traceId)

private suspend fun repositoryCatalog(user: User): RepositoryCatalog = if (useInMemory()) {
    memoryRepositoryCatalog(user.id)
} else {
    DynamoRepositoryCatalog(user.id, TimeProvider)
}

val memoryBackend by lazy { MemoryRepositoryBackend() }

private fun memoryRepositoryCatalog(userId: String) = MemoryRepositoryCatalog(userId, memoryBackend, TimeProvider)

suspend fun userRepository(userId: String): UserRepository = if (useInMemory()) {
    memoryRepositoryCatalog(userId).userRepository
} else {
    DynamoUserRepository(userId, TimeProvider)
}

fun useInMemory() = Process.getEnv("COUPLING_IN_MEMORY") == "true"
