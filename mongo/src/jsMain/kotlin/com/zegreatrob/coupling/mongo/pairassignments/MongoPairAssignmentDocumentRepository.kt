package com.zegreatrob.coupling.mongo.pairassignments

import com.soywiz.klock.js.toDate
import com.soywiz.klock.js.toDateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.mongo.DbRecordDeleteSyntax
import com.zegreatrob.coupling.mongo.DbRecordLoadSyntax
import com.zegreatrob.coupling.mongo.DbRecordSaveSyntax
import com.zegreatrob.coupling.mongo.pin.PinToDbSyntax
import com.zegreatrob.coupling.mongo.player.PlayerToDbSyntax
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import kotlin.js.Date
import kotlin.js.Json
import kotlin.js.json

interface MongoPairAssignmentDocumentRepository : PairAssignmentDocumentRepository,
    PlayerToDbSyntax,
    PinToDbSyntax,
    DbRecordSaveSyntax,
    DbRecordLoadSyntax,
    DbRecordDeleteSyntax {

    val jsRepository: dynamic

    override suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) =
        tribeIdPairAssignmentDocument
            .toDbJson()
            .save(jsRepository.historyCollection)

    override suspend fun delete(tribeId: TribeId, pairAssignmentDocumentId: PairAssignmentDocumentId) = deleteEntity(
        id = pairAssignmentDocumentId.value,
        collection = jsRepository.historyCollection,
        entityName = "Pair Assignments",
        toDomain = { toPairAssignmentDocument() },
        toDbJson = { toDbJson() }
    )

    override suspend fun getPairAssignmentRecords(tribeId: TribeId) =
        findByQuery(json("tribe" to tribeId.value), jsRepository.historyCollection)
            .map { json -> json.toDbRecord(json.toPairAssignmentDocument()) }
            .sortedByDescending { it.data.document.date }

    private fun TribeIdPairAssignmentDocument.toDbJson() = json(
        "id" to document.id?.value,
        "date" to document.date.toDate(),
        "pairs" to document.toDbJsPairs(),
        "tribe" to tribeId.value
    )

    private fun PairAssignmentDocument.toDbJsPairs() = pairs.map {
        json(
            "pins" to it.pins.map { pin -> pin.toDbJson() }.toTypedArray(),
            "players" to it.players.map { player -> player.toJson() }.toTypedArray()
        )
    }
        .toTypedArray()

    private fun PinnedPlayer.toJson(): Json = player.toDbJson().apply { this["pins"] = pins.toDbJson() }

    private fun List<Pin>.toDbJson(): Array<Json> = map { it.toDbJson() }
        .toTypedArray()

    private fun Json.toPairAssignmentDocument() = TribeIdPairAssignmentDocument(
        TribeId(this["tribe"].unsafeCast<String>()),
        PairAssignmentDocument(
            id = idStringValue()
                .let(::PairAssignmentDocumentId),
            date = this["date"].let { if (it is String) Date(it) else it.unsafeCast<Date>() }.toDateTime(),
            pairs = this["pairs"].unsafeCast<Array<Any>?>()?.map { pairFromRaw(it) } ?: listOf()
        )
    )

    private fun Json.idStringValue() = let { this["id"].unsafeCast<Json?>() ?: this["_id"] }.toString()

    private fun pairFromRaw(pair: Any): PinnedCouplingPair {
        return if (pair is Array<*>) {
            pair.unsafeCast<Array<Json>>().map {
                PinnedPlayer(
                    it.fromDbToPlayer(),
                    it["pins"].unsafeCast<Array<Json>?>()?.toPins() ?: emptyList()
                )
            }.toPairs()
        } else {
            val pairObject = pair.unsafeCast<Json>()
            val pins = pairObject["pins"].unsafeCast<Array<Json>>().toDbPins()
            pairObject["players"].unsafeCast<Array<Json>>().map {
                PinnedPlayer(
                    it.fromDbToPlayer(),
                    it["pins"].unsafeCast<Array<Json>?>()?.toPins() ?: emptyList()
                )
            }.toPairs().copy(pins = pins)
        }
    }

    private fun Array<Json>.toPins() = map {
        Pin(
            _id = it["id"]?.toString() ?: it["_id"]?.toString(),
            name = it["name"]?.toString()
        )
    }

    private fun List<PinnedPlayer>.toPairs() =
        PinnedCouplingPair(this)
}
