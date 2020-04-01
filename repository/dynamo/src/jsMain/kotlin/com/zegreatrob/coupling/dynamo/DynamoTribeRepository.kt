package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import kotlin.js.Json

class DynamoTribeRepository private constructor(override val userEmail: String, override val clock: TimeProvider) :
    TribeRepository,
    DynamoRecordJsonMapping,
    UserEmailSyntax,
    ClockSyntax,
    RecordSyntax,
    DynamoTribeJsonMapping {

    companion object : DynamoTableNameSyntax,
        CreateTableParamProvider,
        DynamoItemGetSyntax,
        DynamoItemPutSyntax,
        DynamoQueryItemListGetSyntax,
        DynamoItemDeleteSyntax,
        ListLatestRecordSyntax,
        DynamoRepositoryCreatorSyntax<DynamoTribeRepository>,
        DynamoDBSyntax by DynamoDbProvider {
        override val tableName = "TRIBE"
        override val construct = ::DynamoTribeRepository
    }

    override suspend fun getTribeRecord(tribeId: TribeId) = performGetSingleItemQuery(tribeId.value)
        ?.let { it.toRecord(it.toTribe()) }

    override suspend fun getTribes() = scanAllRecords()
        .map { it.toRecord(it.toTribe()) }
        .sortedBy { it.timestamp }
        .groupBy { it.data.id }
        .map { it.value.last() }
        .filterNot { it.isDeleted }

    override suspend fun save(tribe: Tribe) = performPutItem(tribe.toRecord().asDynamoJson())

    override suspend fun delete(tribeId: TribeId) = performDelete(
        tribeId.value,
        null,
        now(),
        { asTribeRecord() },
        { asDynamoJson() }
    )

    suspend fun getTribeRecords() = scanAllRecords()
        .sortByRecordTimestamp()
        .map { it.asTribeRecord() }

    private fun Json.asTribeRecord() = toRecord(toTribe())

    suspend fun saveRawRecord(record: Record<Tribe>) = performPutItem(record.asDynamoJson())

}
