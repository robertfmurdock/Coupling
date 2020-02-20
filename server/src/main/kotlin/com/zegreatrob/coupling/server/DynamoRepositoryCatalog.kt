package com.zegreatrob.coupling.server

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.dynamo.*
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.compound.*
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.user.UserRepository

class DynamoRepositoryCatalog private constructor(
    override val userEmail: String,
    override val clock: TimeProvider,
    override val tribeRepository: TribeRepository,
    override val playerRepository: PlayerEmailRepository,
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository,
    override val pinRepository: PinRepository,
    override val userRepository: UserRepository
) :
    RepositoryCatalog,
    UserEmailSyntax,
    ClockSyntax {

    companion object {
        suspend operator fun invoke(userEmail: String, clock: TimeProvider): DynamoRepositoryCatalog {
            val tribeRepository = DynamoTribeRepository(userEmail, clock)
            val playerRepository = DynamoPlayerRepository(userEmail, clock)
            val pairAssignmentDocumentRepository = DynamoPairAssignmentDocumentRepository(userEmail, clock)
            val pinRepository = DynamoPinRepository(userEmail, clock)
            val userRepository = DynamoUserRepository(userEmail, clock)
            return DynamoRepositoryCatalog(
                userEmail,
                clock,
                tribeRepository,
                playerRepository,
                pairAssignmentDocumentRepository,
                pinRepository,
                userRepository
            )
        }
    }

}

suspend fun compoundRepositoryCatalog(
    userCollection: dynamic,
    jsRepository: dynamic,
    user: User
): CompoundRepositoryCatalog {
    val mongoRepositoryCatalog = MongoRepositoryCatalog(userCollection, jsRepository, user)
    val dynamoRepositoryCatalog = DynamoRepositoryCatalog(user.email, TimeProvider)
    return CompoundRepositoryCatalog(
        catalog1 = mongoRepositoryCatalog,
        catalog2 = dynamoRepositoryCatalog
    )
}

class CompoundRepositoryCatalog private constructor(
    override val tribeRepository: TribeRepository,
    override val playerRepository: PlayerEmailRepository,
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository,
    override val pinRepository: PinRepository,
    override val userRepository: UserRepository
) : RepositoryCatalog {

    companion object {
        suspend operator fun invoke(
            catalog1: RepositoryCatalog,
            catalog2: RepositoryCatalog
        ): CompoundRepositoryCatalog {
            val tribeRepository = CompoundTribeRepository(catalog1.tribeRepository, catalog2.tribeRepository)
            val playerRepository = CompoundPlayerRepository(catalog1.playerRepository, catalog2.playerRepository)
            val pairAssignmentDocumentRepository = CompoundPairAssignmentDocumentRepository(
                catalog1.pairAssignmentDocumentRepository,
                catalog2.pairAssignmentDocumentRepository
            )
            val pinRepository = CompoundPinRepository(catalog1.pinRepository, catalog2.pinRepository)
            val userRepository = CompoundUserRepository(catalog1.userRepository, catalog2.userRepository)

            return CompoundRepositoryCatalog(
                tribeRepository,
                playerRepository,
                pairAssignmentDocumentRepository,
                pinRepository,
                userRepository
            )
        }
    }

}