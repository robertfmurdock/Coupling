package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.TribeId
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

    override suspend fun save(tribeIdPin: TribeIdPin) = performPutItem(tribeIdPin.toDynamoJson())

    override suspend fun getPins(tribeId: TribeId) = tribeId.scanForItemList().map {
        val pin = it.toPin()
        it.toRecord(TribeIdPin(tribeId, pin))
    }

    override suspend fun deletePin(tribeId: TribeId, pinId: String) = performDelete(pinId, recordJson(), tribeId)

}
