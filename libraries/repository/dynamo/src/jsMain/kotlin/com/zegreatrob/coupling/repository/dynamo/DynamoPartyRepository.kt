package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.ClockProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.party.PartyRepository
import kotlin.js.Json
import kotlin.time.Clock

class DynamoPartyRepository private constructor(override val userId: UserId, override val clock: Clock) :
    PartyRepository,
    DynamoRecordJsonMapping,
    UserIdProvider,
    ClockProvider,
    RecordSyntax,
    DynamoPartyJsonMapping {

    companion object :
        DynamoTableNameSyntax,
        CreateTableParamProvider,
        DynamoItemGetSyntax,
        DynamoItemPutSyntax,
        DynamoQueryItemListGetSyntax,
        DynamoItemPutDeleteRecordSyntax,
        ListLatestRecordSyntax,
        DynamoRepositoryCreatorSyntax<DynamoPartyRepository>(),
        DynamoDBSyntax by DynamoDbProvider {
        override val tableName = "TRIBE"
        override val construct = ::DynamoPartyRepository
    }

    override suspend fun getDetails(partyId: PartyId) = performGetSingleItemQuery(partyId.value.toString())
        ?.let { it.toRecord(it.toParty()) }

    override suspend fun save(integration: PartyElement<PartyIntegration>) = performPutItem(
        integration.toRecord().asDynamoJson(),
    )

    override suspend fun getIntegration(partyId: PartyId): Record<PartyIntegration>? = performGetSingleItemQuery(
        "${DynamoPartyJsonMapping.INTEGRATION_CONSTANT}${partyId.value}",
    )?.let { it.toRecord(it.toIntegration()) }

    override suspend fun loadParties() = scanAllRecords()
        .map { it.toRecord(it.toParty()) }
        .sortedBy { it.timestamp }
        .groupBy { it.data.id }
        .map { it.value.last() }
        .filterNot { it.isDeleted }

    override suspend fun save(party: PartyDetails) = performPutItem(party.toRecord().asDynamoJson())

    override suspend fun deleteIt(partyId: PartyId) = performDelete(
        partyId.value.toString(),
        null,
        now(),
        { asPartyRecord() },
        { asDynamoJson() },
    )

    suspend fun getPartyRecords() = scanAllRecords()
        .sortByRecordTimestamp()
        .map { it.asPartyRecord() }

    private fun Json.asPartyRecord() = toRecord(toParty())

    suspend fun saveRawRecord(record: Record<PartyDetails>) = performPutItem(record.asDynamoJson())
}
