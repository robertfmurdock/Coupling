package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.pin.PinRepository
import kotlinx.datetime.Clock
import kotlin.js.Json

class DynamoPinRepository private constructor(override val userId: String, override val clock: Clock) :
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

    override suspend fun save(partyPin: PartyElement<Pin>) = performPutItem(
        partyPin.copy(element = with(partyPin.element) { copy(id = id) })
            .toRecord()
            .asDynamoJson(),
    )

    suspend fun saveRawRecord(record: PartyRecord<Pin>) = performPutItem(
        record.asDynamoJson(),
    )

    override suspend fun getPins(partyId: PartyId) = partyId.queryForItemList().map {
        it.toRecord()
    }

    private fun Json.toRecord(): Record<PartyElement<Pin>> {
        val partyId = this["tribeId"].unsafeCast<String>().let(::PartyId)
        val pin = toPin()
        return toRecord(partyId.with(pin))
    }

    override suspend fun deletePin(partyId: PartyId, pinId: String) = performDelete(
        pinId,
        partyId,
        now(),
        { toRecord() },
        { asDynamoJson() },
    )

    suspend fun getPinRecords(partyId: PartyId) = partyId.logAsync("itemList") {
        queryAllRecords(partyId.itemListQueryParams())
    }.map {
        val pin = it.toPin()
        it.toRecord(partyId.with(pin))
    }
}
