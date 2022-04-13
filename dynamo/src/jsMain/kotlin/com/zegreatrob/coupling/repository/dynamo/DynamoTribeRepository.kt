package com.zegreatrob.coupling.repository.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserIdSyntax
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import kotlin.js.Json

class DynamoTribeRepository private constructor(override val userId: String, override val clock: TimeProvider) :
    TribeRepository,
    DynamoRecordJsonMapping,
    UserIdSyntax,
    ClockSyntax,
    RecordSyntax,
    DynamoTribeJsonMapping {

    companion object : DynamoTableNameSyntax,
        com.zegreatrob.coupling.repository.dynamo.CreateTableParamProvider,
        DynamoItemGetSyntax,
        DynamoItemPutSyntax,
        DynamoQueryItemListGetSyntax,
        DynamoItemPutDeleteRecordSyntax,
        ListLatestRecordSyntax,
        DynamoRepositoryCreatorSyntax<DynamoTribeRepository>,
        DynamoDBSyntax by DynamoDbProvider {
        override val tableName = "TRIBE"
        override val construct = ::DynamoTribeRepository
    }

    override suspend fun getTribeRecord(partyId: PartyId) = performGetSingleItemQuery(partyId.value)
        ?.let { it.toRecord(it.toParty()) }

    override suspend fun getTribes() = scanAllRecords()
        .map { it.toRecord(it.toParty()) }
        .sortedBy { it.timestamp }
        .groupBy { it.data.id }
        .map { it.value.last() }
        .filterNot { it.isDeleted }

    override suspend fun save(party: Party) = performPutItem(party.toRecord().asDynamoJson())

    override suspend fun delete(partyId: PartyId) = performDelete(
        partyId.value,
        null,
        now(),
        { asTribeRecord() },
        { asDynamoJson() }
    )

    suspend fun getTribeRecords() = scanAllRecords()
        .sortByRecordTimestamp()
        .map { it.asTribeRecord() }

    private fun Json.asTribeRecord() = toRecord(toParty())

    suspend fun saveRawRecord(record: Record<Party>) = performPutItem(record.asDynamoJson())

}
