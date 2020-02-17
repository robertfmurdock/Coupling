package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import kotlin.js.json

class DynamoTribeRepository private constructor() : TribeRepository,
    DynamoItemGetSyntax,
    DynamoItemPutSyntax,
    DynamoItemListGetSyntax,
    DynamoItemDeleteSyntax,
    DynamoTribeJsonMapping,
    DynamoTableNameSyntax by Companion,
    DynamoDBSyntax by DynamoDbProvider {

    companion object : DynamoTableNameSyntax, CreateTableParamProvider,
        DynamoRepositoryCreatorSyntax<DynamoTribeRepository>,
        DynamoDBSyntax by DynamoDbProvider {
        override val tableName = "TRIBE"
        override val construct = ::DynamoTribeRepository
    }

    override suspend fun getTribe(tribeId: TribeId) = performGetSingleItemQuery(tribeId.value)?.toTribe()

    override suspend fun getTribes() = scanForItemList(scanParams()).map { it.toTribe() }

    private fun scanParams() = json("TableName" to tableName)

    override suspend fun save(tribe: Tribe) = performPutItem(tribe.asDynamoJson())

    override suspend fun delete(tribeId: TribeId) = performDelete(tribeId.value)

}
