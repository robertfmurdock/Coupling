package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSetId
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.js.Json
import kotlin.time.Clock

class DynamoPairAssignmentDocumentRepository private constructor(
    override val userId: UserId,
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

    override suspend fun save(partyPairDocument: PartyElement<PairingSet>) = performPutItem(
        partyPairDocument
            .toRecord()
            .asDynamoJson(),
    )

    suspend fun saveRawRecord(record: PartyRecord<PairingSet>) = performPutItem(
        record.asDynamoJson(),
    )

    override suspend fun loadPairAssignments(partyId: PartyId) = partyId.queryForItemList()
        .mapNotNull { toRecord(it) }
        .sortedByDescending { it.data.document.date }

    override suspend fun getCurrentPairAssignments(partyId: PartyId) = partyId.queryForItemList()
        .mapNotNull { toRecord(it) }
        .maxByOrNull { it.data.document.date }

    override suspend fun deleteIt(partyId: PartyId, pairingSetId: PairingSetId) = performDelete(
        pairingSetId.value.toString(),
        partyId,
        now(),
        ::toRecord,
    ) { asDynamoJson() }

    suspend fun getRecords(partyId: PartyId): List<PartyRecord<PairingSet>> = partyId.logAsync("getPairAssignmentRecords") {
        queryAllRecords(partyId.itemListQueryParams())
    }
        .mapNotNull { toRecord(it) }
        .sortedByDescending { it.timestamp }

    private fun toRecord(json: Json): PartyRecord<PairingSet>? = json.toPairAssignmentDocument()
        ?.let { json.tribeId().with(it) }
        ?.let { json.toRecord(it) }

    @OptIn(ExperimentalKotoolsTypesApi::class)
    private fun Json.tribeId() = this["tribeId"].unsafeCast<String>().let(::PartyId)
}
