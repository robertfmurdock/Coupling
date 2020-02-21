package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.ClockSyntax
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import kotlin.js.json

class DynamoTribeRepository private constructor(override val userEmail: String, override val clock: TimeProvider) :
    TribeRepository,
    DynamoRecordJsonMapping,
    UserEmailSyntax,
    ClockSyntax,
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

    override suspend fun getTribe(tribeId: TribeId) = performGetSingleItemQuery(tribeId.value)?.toTribe()

    override suspend fun getTribes() = performScan(queryParams())
        .fullList()
        .map { it.toRecord(it.toTribe()) }
        .filterNot { it.isDeleted }

    private fun queryParams() = json("TableName" to tableName)

    override suspend fun save(tribe: Tribe) = performPutItem(tribe.asDynamoJson().add(recordJson(now())))

    override suspend fun delete(tribeId: TribeId) = performDelete(tribeId.value, recordJson(now()))

}
