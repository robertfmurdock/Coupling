package com.zegreatrob.coupling.export

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.export.external.monk.MonkDb
import com.zegreatrob.coupling.export.external.monk.default
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.AuthenticatedUserEmailSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.mongo.pairassignments.MongoPairAssignmentDocumentRepository
import com.zegreatrob.coupling.mongo.pin.MongoPinRepository
import com.zegreatrob.coupling.mongo.player.MongoPlayerRepository
import com.zegreatrob.coupling.mongo.tribe.MongoTribeRepository
import com.zegreatrob.coupling.mongo.user.MongoUserRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.js.Json
import kotlin.js.json

fun exportWithMongo() {
    val monkDb = default(mongoUrl())
    val jsRepo = jsRepository(monkDb)
    val repositoryCatalog = MongoRepositoryCatalog(
        jsRepo.get("userCollection"),
        jsRepo,
        user
    );

    MainScope().launch {
        outputUsers(repositoryCatalog)
        repositoryCatalog.outputTribes()
    }.invokeOnCompletion {
        monkDb.close()
    }
}

private suspend fun MongoRepositoryCatalog.outputTribes() = getTribeRecordList()
    .groupBy { it.data.id }
    .entries.sortedBy { it.key.value }
    .forEach { tribeGroup ->
        collectTribeData(this, tribeGroup.key, tribeGroup.value)
            .print()
    }

private suspend fun collectTribeData(
    repositoryCatalog: MongoRepositoryCatalog,
    tribeId: TribeId,
    tribeRecords: List<Record<Tribe>>
) = json(
    "tribeId" to tribeId.value,
    "tribeRecords" to tribeRecords.map { record -> record.toJson().add(record.data.toJson()) },
    "playerRecords" to repositoryCatalog.getPlayerRecords(tribeId).map { record ->
        record.toJson().add(record.data.element.toJson())
    },
    "pairAssignmentRecords" to repositoryCatalog.getPairAssignmentRecords(tribeId).map { record ->
        record.toJson().add(record.data.element.toJson())
    },
    "pinRecords" to repositoryCatalog.getPinRecords(tribeId).map { record ->
        record.toJson().add(record.data.element.toJson())
    }
)

private fun Json.print() = println(JSON.stringify(this))
private suspend fun outputUsers(repositoryCatalog: MongoRepositoryCatalog) {
    repositoryCatalog.getUserRecords()
        .groupBy { it.data.email }
        .entries.sortedBy { it.key }.forEach {
        json("userEmail" to it.key,
            "userRecords" to it.value.map { record ->
                record.toJson().add(record.data.toJson())
            })
            .print()
    }
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

fun jsRepository(db: MonkDb) =
    json(
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