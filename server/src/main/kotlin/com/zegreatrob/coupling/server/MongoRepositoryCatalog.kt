package com.zegreatrob.coupling.server

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.user.AuthenticatedUserEmailSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.mongo.pairassignments.MongoPairAssignmentDocumentRepository
import com.zegreatrob.coupling.mongo.pin.MongoPinRepository
import com.zegreatrob.coupling.mongo.player.MongoPlayerRepository
import com.zegreatrob.coupling.mongo.tribe.MongoTribeRepository
import com.zegreatrob.coupling.mongo.user.MongoUserRepository

class MongoRepositoryCatalog(
    override val userCollection: dynamic,
    override val jsRepository: dynamic,
    override val user: User
) : RepositoryCatalog,
    MongoTribeRepository,
    ServerPlayerRepository,
    MongoPlayerRepository,
    MongoPairAssignmentDocumentRepository,
    MongoPinRepository,
    MongoUserRepository,
    AuthenticatedUserEmailSyntax {
    override val userRepository = this
    override val tribeRepository = this
    override val playerRepository = this
    override val pairAssignmentDocumentRepository = this
    override val pinRepository = this
    override val clock = TimeProvider
}
