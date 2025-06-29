package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinId
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.pin.PinRepository
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.js.Json
import kotlin.time.Clock

class DynamoPinRepository private constructor(override val userId: UserId, override val clock: Clock) :
    PinRepository,
    UserIdProvider,
    DynamoPinJsonMapping,
    RecordSyntax {

    companion object :
        DynamoRepositoryCreatorSyntax<DynamoPinRepository>(),
        PartyCreateTableParamProvider,
        DynamoItemPutSyntax,
        PartyIdDynamoItemListGetSyntax,
        DynamoItemPutDeleteRecordSyntax,
        DynamoDBSyntax by DynamoDbProvider {
        override val construct = ::DynamoPinRepository
        override val tableName = "PIN"
    }

    override suspend fun save(partyPin: PartyElement<Pin>) = performPutItem(partyPin.toRecord().asDynamoJson())

    suspend fun saveRawRecord(record: PartyRecord<Pin>) = performPutItem(
        record.asDynamoJson(),
    )

    override suspend fun getPins(partyId: PartyId) = partyId.queryForItemList().mapNotNull {
        it.toRecord()
    }

    @OptIn(ExperimentalKotoolsTypesApi::class)
    private fun Json.toRecord(): Record<PartyElement<Pin>>? {
        val partyId = this["tribeId"].unsafeCast<String>().let(::PartyId)
        val pin = toPin() ?: return null
        return toRecord(partyId.with(pin))
    }

    override suspend fun deletePin(partyId: PartyId, pinId: PinId) = performDelete(
        pinId.value.toString(),
        partyId,
        now(),
        { toRecord() },
        { asDynamoJson() },
    )

    suspend fun getPinRecords(partyId: PartyId) = partyId.logAsync("itemList") {
        queryAllRecords(partyId.itemListQueryParams())
    }.mapNotNull {
        val pin = it.toPin() ?: return@mapNotNull null
        it.toRecord(partyId.with(pin))
    }
}
