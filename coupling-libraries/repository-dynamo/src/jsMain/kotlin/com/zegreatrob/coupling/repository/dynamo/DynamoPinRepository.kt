package com.zegreatrob.coupling.repository.dynamo

import com.benasher44.uuid.uuid4
import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.TribeElement
import com.zegreatrob.coupling.model.tribe.PartyId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.UserIdSyntax
import com.zegreatrob.coupling.repository.pin.PinRepository
import kotlin.js.Json

class DynamoPinRepository private constructor(override val userId: String, override val clock: TimeProvider) :
    PinRepository,
    UserIdSyntax,
    DynamoPinJsonMapping,
    RecordSyntax {

    companion object : DynamoRepositoryCreatorSyntax<DynamoPinRepository>,
        TribeCreateTableParamProvider,
        DynamoItemPutSyntax,
        TribeIdDynamoItemListGetSyntax,
        DynamoItemPutDeleteRecordSyntax,
        DynamoDBSyntax by DynamoDbProvider {
        override val construct = ::DynamoPinRepository
        override val tableName = "PIN"
    }

    override suspend fun save(tribeIdPin: TribeIdPin) = performPutItem(
        tribeIdPin.copy(element = with(tribeIdPin.element) { copy(id = id ?: "${uuid4()}") })
            .toRecord()
            .asDynamoJson()
    )

    suspend fun saveRawRecord(record: TribeRecord<Pin>) = performPutItem(
        record.asDynamoJson()
    )

    override suspend fun getPins(partyId: PartyId) = partyId.queryForItemList().map {
        it.toRecord()
    }

    private fun Json.toRecord(): Record<TribeElement<Pin>> {
        val partyId = this["tribeId"].unsafeCast<String>().let(::PartyId)
        val pin = toPin()
        return toRecord(partyId.with(pin))
    }

    override suspend fun deletePin(partyId: PartyId, pinId: String) = performDelete(
        pinId,
        partyId,
        now(),
        { toRecord() },
        { asDynamoJson() }
    )

    suspend fun getPinRecords(partyId: PartyId) = partyId.logAsync("itemList") {
        performQuery(partyId.itemListQueryParams()).itemsNode()
    }.map {
        val pin = it.toPin()
        it.toRecord(partyId.with(pin))
    }


}
