package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinRepository

class DynamoPinRepository private constructor() : PinRepository {

    companion object : DynamoRepositoryCreatorSyntax<DynamoPinRepository>,
        TribeCreateTableParamProvider,
        DynamoItemPutSyntax,
        TribeIdDynamoItemListGetSyntax,
        DynamoItemDeleteSyntax,
        DynamoPinJsonMapping,
        DynamoDBSyntax by DynamoDbProvider {
        override val construct = ::DynamoPinRepository
        override val tableName = "PIN"
    }

    override suspend fun save(tribeIdPin: TribeIdPin) = performPutItem(tribeIdPin.toDynamoJson())

    override suspend fun getPins(tribeId: TribeId): List<Pin> = tribeId.scanForItemList().map { it.toPin() }

    override suspend fun deletePin(tribeId: TribeId, pinId: String) = performDelete(pinId, tribeId)

}
