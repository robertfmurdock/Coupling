package com.zegreatrob.coupling.repository.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.UserIdSyntax
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import kotlin.js.Json


class DynamoPairAssignmentDocumentRepository private constructor(
    override val userId: String,
    override val clock: TimeProvider
) : PairAssignmentDocumentRepository, UserIdSyntax, RecordSyntax, DynamoPairAssignmentDocumentJsonMapping {

    companion object : DynamoRepositoryCreatorSyntax<DynamoPairAssignmentDocumentRepository>,
        TribeCreateTableParamProvider,
        DynamoItemPutSyntax,
        DynamoItemPutDeleteRecordSyntax,
        TribeIdDynamoItemListGetSyntax,
        DynamoDBSyntax by DynamoDbProvider {
        override val construct = ::DynamoPairAssignmentDocumentRepository
        override val tableName = "PAIR_ASSIGNMENTS"
    }

    override suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) = performPutItem(
        tribeIdPairAssignmentDocument
            .toRecord()
            .asDynamoJson()
    )

    suspend fun saveRawRecord(record: TribeRecord<PairAssignmentDocument>) = performPutItem(
        record.asDynamoJson()
    )

    override suspend fun getPairAssignments(tribeId: TribeId) = tribeId.queryForItemList()
        .map { toRecord(it) }
        .sortedByDescending { it.data.document.date }

    override suspend fun getCurrentPairAssignments(tribeId: TribeId) = tribeId.queryForItemList()
        .map { toRecord(it) }
        .maxByOrNull { it.data.document.date }

    override suspend fun delete(tribeId: TribeId, pairAssignmentDocumentId: PairAssignmentDocumentId) =
        performDelete(
            pairAssignmentDocumentId.value, tribeId, now(), ::toRecord
        ) { asDynamoJson() }

    suspend fun getRecords(tribeId: TribeId): List<TribeRecord<PairAssignmentDocument>> =
        tribeId.logAsync("getPairAssignmentRecords") {
            performQuery(tribeId.itemListQueryParams()).itemsNode()
        }
            .map { toRecord(it) }
            .sortedByDescending { it.timestamp }

    private fun toRecord(json: Json): TribeRecord<PairAssignmentDocument> = json.toRecord(
        json.tribeId().with(json.toPairAssignmentDocument())
    )

    private fun Json.tribeId() = this["tribeId"].unsafeCast<String>().let(::TribeId)


}
