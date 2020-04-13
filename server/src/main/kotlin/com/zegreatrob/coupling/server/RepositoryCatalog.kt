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

suspend fun commandDispatcher(
    user: User,
    scope: CoroutineScope,
    traceId: Uuid?
): CommandDispatcher {
    val dynamoRepositoryCatalog = DynamoRepositoryCatalog(user.id, TimeProvider)
    return CommandDispatcher(user, dynamoRepositoryCatalog, scope, traceId)
}

suspend fun userRepository(userEmail: String) = DynamoUserRepository(userEmail, TimeProvider)
