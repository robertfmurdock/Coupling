package com.zegreatrob.coupling.mongo.pairassignments

import com.soywiz.klock.js.toDate
import com.soywiz.klock.js.toDateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.mongo.DbRecordDeleteSyntax
import com.zegreatrob.coupling.mongo.DbRecordLoadSyntax
import com.zegreatrob.coupling.mongo.DbRecordSaveSyntax
import com.zegreatrob.coupling.mongo.player.PlayerToDbSyntax
import kotlin.js.*

interface MongoPairAssignmentDocumentRepository : PairAssignmentDocumentRepository,
    PlayerToDbSyntax,
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

    override suspend fun getPairAssignments(tribeId: TribeId): List<PairAssignmentDocument> =
        findByQuery(json("tribe" to tribeId.value), jsRepository.historyCollection)
            .map { json -> json.toPairAssignmentDocument().document }
            .sortedByDescending { it.date }

    private fun TribeIdPairAssignmentDocument.toDbJson() = json(
        "id" to document.id?.value,
        "date" to document.date.toDate(),
        "pairs" to document.toDbJsPairs(),
        "tribe" to tribeId.value
    )

    private fun PairAssignmentDocument.toDbJsPairs() = pairs.map {
        it.players
            .map { player -> player.toJson() }
            .toTypedArray()
    }
        .toTypedArray()

    private fun PinnedPlayer.toJson(): Json = player.toDbJson().apply { this["pins"] = pins.toDbJson() }

    private fun List<Pin>.toDbJson(): Array<Json> = map { it.toDbJson() }
        .toTypedArray()

    private fun Pin.toDbJson() = json("id" to _id, "tribe" to tribe, "name" to name)

    private fun Json.toPairAssignmentDocument() =
        TribeIdPairAssignmentDocument(
            TribeId(this["tribe"].unsafeCast<String>()),
            PairAssignmentDocument(
                date = this["date"].let { if (it is String) Date(it) else it.unsafeCast<Date>() }.toDateTime(),
                pairs = this["pairs"].unsafeCast<Array<Array<Json>>?>()?.map(::pairFromArray) ?: listOf(),
                id = idStringValue()
                    .let(::PairAssignmentDocumentId)
            )
        )

    private fun Json.idStringValue() = let { this["id"].unsafeCast<Json?>() ?: this["_id"] }.toString()

    @JsName("pairFromArray")
    fun pairFromArray(array: Array<Json>) = array.map {
        PinnedPlayer(
            it.fromDbToPlayer(),
            it["pins"].unsafeCast<Array<Json>?>()?.toPins() ?: emptyList()
        )
    }.toPairs()

    private fun Array<Json>.toPins() = map {
        Pin(
            _id = it["id"]?.toString() ?: it["_id"]?.toString(),
            name = it["name"]?.toString(),
            tribe = it["tribe"]?.toString()
        )
    }

    private fun List<PinnedPlayer>.toPairs() =
        PinnedCouplingPair(this)
}
