package com.zegreatrob.coupling.export

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.export.external.monk.MonkDb
import com.zegreatrob.coupling.model.user.AuthenticatedUserEmailSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.mongo.pairassignments.MongoPairAssignmentDocumentRepository
import com.zegreatrob.coupling.mongo.pin.MongoPinRepository
import com.zegreatrob.coupling.mongo.player.MongoPlayerRepository
import com.zegreatrob.coupling.mongo.tribe.MongoTribeRepository
import com.zegreatrob.coupling.mongo.user.MongoUserRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import kotlin.js.json


val user = User("EXPORT_USER", "robert.f.murdock@gmail.com", emptySet())

fun main() {
    val monkDb = com.zegreatrob.coupling.export.external.monk.default(mongoUrl())
    val jsRepo = jsRepository(monkDb)
    val repositoryCatalog = MongoRepositoryCatalog(jsRepo.get("userCollection"), jsRepo, user);


    
}

class MongoRepositoryCatalog(
    override val userCollection: dynamic,
    override val jsRepository: dynamic,
    override val user: User
) : MongoTribeRepository,
    PlayerEmailRepository,
    MongoPlayerRepository,
    MongoPairAssignmentDocumentRepository,
    MongoPinRepository,
    MongoUserRepository,
    AuthenticatedUserEmailSyntax {
    override val clock = TimeProvider
}

fun jsRepository(db: MonkDb) = json(
    "playersCollection" to db.get("players"),
    "historyCollection" to db.get("history"),
    "tribesCollection" to db.get("tribes"),
    "userCollection" to db.get("users"),
    "pinCollection" to db.get("pins")
)

fun mongoUrl(): String {
    val env = js("process.env")
    return env.MONGOHQ_URL_MONGOURL.unsafeCast<String?>()
        ?: env.MONGOHQ_URL.unsafeCast<String?>()
        ?: "mongodb://localhost/Coupling"
}