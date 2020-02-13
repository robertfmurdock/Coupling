package com.zegreatrob.coupling.dynamo

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import kotlin.js.Json
import kotlin.js.json


class DynamoPairAssignmentDocumentRepository private constructor() : PairAssignmentDocumentRepository {

    companion object : DynamoRepositoryCreatorSyntax<DynamoPairAssignmentDocumentRepository>,
        TribeCreateTableParamProvider,
        DynamoItemPutSyntax,
        TribeIdDynamoItemListGetSyntax,
        DynamoPlayerJsonMapping,
        DynamoDBSyntax by DynamoDbProvider {
        override val construct = ::DynamoPairAssignmentDocumentRepository
        override val tableName = "PAIR_ASSIGNMENTS"
    }

    override suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) = performPutItem(
        tribeIdPairAssignmentDocument.toDynamoJson()
    )

    override suspend fun getPairAssignments(tribeId: TribeId): List<PairAssignmentDocument> = tribeId.scanForItemList()
        .map { it.toPairAssignmentDocument() }
        .sortedByDescending { it.date }

    override suspend fun delete(tribeId: TribeId, pairAssignmentDocumentId: PairAssignmentDocumentId): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun TribeIdPairAssignmentDocument.toDynamoJson() = json(
        "tribeId" to tribeId.value,
        "id" to (document.id?.value ?: "${uuid4()}"),
        "timestamp" to DateTime.now().isoWithMillis()
    ).add(
        json(
            "date" to "${document.date.unixMillisLong}",
            "pairs" to document.pairs.map { it.toDynamoJson() }
                .toTypedArray()
        )
    )

    private fun PinnedCouplingPair.toDynamoJson() = json(
        "pins" to pins.map { it.toDynamoJson() }.toTypedArray(),
        "players" to players.map { it.toDynamoJson() }.toTypedArray()
    )

    private fun Pin.toDynamoJson() = json(
        "id" to _id,
        "name" to name,
        "icon" to icon
    )

    private fun PinnedPlayer.toDynamoJson() = json(
        "pins" to pins.map { it.toDynamoJson() }.toTypedArray(),
        "player" to player.toDynamoJson()
    )

    private fun Json.toPairAssignmentDocument() = PairAssignmentDocument(
        id = getDynamoStringValue("id")!!.let(::PairAssignmentDocumentId),
        date = DateTime(getDynamoStringValue("date")!!.toLong()),
        pairs = getDynamoListValue("pairs")?.map { pair ->
            PinnedCouplingPair(
                players = pair.getDynamoListValue("players")
                    ?.map { pinnedPlayerJson ->
                        PinnedPlayer(
                            player = pinnedPlayerJson["player"].unsafeCast<Json>().toPlayer(),
                            pins = emptyList()
                        )
                    } ?: emptyList(),
                pins = pair.getDynamoListValue("pins")
                    ?.map { pinJson ->
                        pinJson.toPin()
                    } ?: emptyList()
            )
        } ?: emptyList()
    )

    private fun Json.toPin() = Pin(
        _id = getDynamoStringValue("id"),
        name = getDynamoStringValue("name"),
        icon = getDynamoStringValue("icon")
    )

}



