package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.pin.PinRepository

class DynamoPinRepository private constructor(override val userEmail: String, override val clock: TimeProvider) :
    PinRepository,
    UserEmailSyntax,
    DynamoPinJsonMapping {

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
        tribeIdPin.copy(element = with(tribeIdPin.element) { copy(_id = _id ?: "${com.benasher44.uuid.uuid4()}") })
            .toDynamoJson()
    )

    override suspend fun getPins(tribeId: TribeId) = tribeId.queryForItemList().map {
        val pin = it.toPin()
        it.toRecord(tribeId.with(pin))
    }

    override suspend fun deletePin(tribeId: TribeId, pinId: String) = performDelete(pinId, recordJson(now()), tribeId)

}
