package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.mongo.user.MongoUserRepository
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
    userCollection: dynamic,
    jsRepository: dynamic,
    user: User,
    scope: CoroutineScope,
    traceId: Uuid?
): CommandDispatcher {
    val repositoryCatalog = MongoRepositoryCatalog(userCollection, jsRepository, user)
    return CommandDispatcher(user, repositoryCatalog, scope, traceId)
}

suspend fun userRepository(userCollection: dynamic, userEmail: String) =
    mongoUserRepository(userCollection, userEmail)

private fun mongoUserRepository(userCollection: dynamic, userEmail: String) = object : MongoUserRepository {
    override val userCollection = userCollection
    override val userEmail = userEmail
    override val clock = TimeProvider
}
