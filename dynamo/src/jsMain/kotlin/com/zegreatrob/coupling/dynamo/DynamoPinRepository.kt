package com.zegreatrob.coupling.dynamo

import com.benasher44.uuid.uuid4
import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.pin.PinRepository
import kotlin.js.json

class DynamoPinRepository private constructor(override val userEmail: String, override val clock: TimeProvider) :
    PinRepository,
    UserEmailSyntax,
    DynamoPinJsonMapping,
    RecordSyntax {

    companion object : DynamoRepositoryCreatorSyntax<DynamoPinRepository>,
        TribeCreateTableParamProvider,
        DynamoItemPutSyntax,
        TribeIdDynamoItemListGetSyntax,
        DynamoItemDeleteSyntax,
        DynamoDBSyntax by DynamoDbProvider {
        override val construct = ::DynamoPinRepository
        override val tableName = "PIN"
    }

    override suspend fun save(tribeIdPin: TribeIdPin) = performPutItem(
        tribeIdPin.copy(element = with(tribeIdPin.element) { copy(_id = _id ?: "${uuid4()}") })
            .toRecord()
            .asDynamoJson()
    )

    suspend fun saveRawRecord(record: TribeRecord<Pin>) = performPutItem(
        record.asDynamoJson()
    )

    override suspend fun getPins(tribeId: TribeId) = tribeId.queryForItemList().map {
        val pin = it.toPin()
        it.toRecord(tribeId.with(pin))
    }

    override suspend fun deletePin(tribeId: TribeId, pinId: String) = performDelete(
        pinId,
        now().let {
            json(
                "timestamp" to it.isoWithMillis(),
                "modifyingUserEmail" to userEmail,
                "timestamp+id" to "${it.isoWithMillis()}+${pinId}"
            )
        },
        tribeId
    )

    suspend fun getPinRecords(tribeId: TribeId) = tribeId.logAsync("itemList") {
        performQuery(tribeId.itemListQueryParams()).itemsNode()
    }.map {
        val pin = it.toPin()
        it.toRecord(tribeId.with(pin))
    }


}
