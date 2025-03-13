package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import kotlinx.datetime.Clock
import kotools.types.text.NotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.js.Json

class DynamoPairAssignmentDocumentRepository private constructor(
    override val userId: String,
    override val clock: Clock,
) : PairAssignmentDocumentRepository,
    UserIdProvider,
    RecordSyntax,
    DynamoPairAssignmentDocumentJsonMapping {

    companion object :
        DynamoRepositoryCreatorSyntax<DynamoPairAssignmentDocumentRepository>(),
        PartyCreateTableParamProvider,
        DynamoItemPutSyntax,
        DynamoItemPutDeleteRecordSyntax,
        PartyIdDynamoItemListGetSyntax,
        DynamoDBSyntax by DynamoDbProvider {
        override val construct = ::DynamoPairAssignmentDocumentRepository
        override val tableName = "PAIR_ASSIGNMENTS"
    }

    override suspend fun save(partyPairDocument: PartyElement<PairAssignmentDocument>) = performPutItem(
        partyPairDocument
            .toRecord()
            .asDynamoJson(),
    )

    suspend fun saveRawRecord(record: PartyRecord<PairAssignmentDocument>) = performPutItem(
        record.asDynamoJson(),
    )

    override suspend fun loadPairAssignments(partyId: PartyId) = partyId.queryForItemList()
        .mapNotNull { toRecord(it) }
        .sortedByDescending { it.data.document.date }

    override suspend fun getCurrentPairAssignments(partyId: PartyId) = partyId.queryForItemList()
        .mapNotNull { toRecord(it) }
        .maxByOrNull { it.data.document.date }

    override suspend fun deleteIt(partyId: PartyId, pairAssignmentDocumentId: PairAssignmentDocumentId) = performDelete(
        pairAssignmentDocumentId.value.toString(),
        partyId,
        now(),
        ::toRecord,
    ) { asDynamoJson() }

    suspend fun getRecords(partyId: PartyId): List<PartyRecord<PairAssignmentDocument>> = partyId.logAsync("getPairAssignmentRecords") {
        queryAllRecords(partyId.itemListQueryParams())
    }
        .mapNotNull { toRecord(it) }
        .sortedByDescending { it.timestamp }

    private fun toRecord(json: Json): PartyRecord<PairAssignmentDocument>? = json.toPairAssignmentDocument()
        ?.let { json.tribeId().with(it) }
        ?.let { json.toRecord(it) }

    @OptIn(ExperimentalKotoolsTypesApi::class)
    private fun Json.tribeId() = this["tribeId"].unsafeCast<String>().let(NotBlankString::create).let(::PartyId)
}
