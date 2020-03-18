package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.user.UserRepository
import kotlinx.coroutines.await
import kotlin.js.json

class DynamoUserRepository private constructor(override val userEmail: String, override val clock: TimeProvider) :
    UserRepository,
    UserEmailSyntax,
    DynamoUserJsonMapping {

    companion object : DynamoDBSyntax by DynamoDbProvider,
        CreateTableParamProvider,
        DynamoItemPutSyntax,
        DynamoQuerySyntax,
        DynamoItemSyntax {
        override val tableName = "USER"
        suspend operator fun invoke(email: String, clock: TimeProvider) = DynamoUserRepository(email, clock)
            .also { ensureTableExists() }
    }

    override suspend fun save(user: User) = performPutItem(user.toRecord().asDynamoJson())

    private fun <T> T.toRecord() = Record(this, userEmail, false, now())


    override suspend fun getUser() = documentClient.scan(emailScanParams()).promise().await()
        .itemsNode()
        .sortByRecordTimestamp()
        .lastOrNull()
        ?.toUserRecord()

    private fun emailScanParams() = json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(":email" to userEmail),
        "FilterExpression" to "email = :email"
    )

    suspend fun saveRawRecord(record: Record<User>) = performPutItem(record.asDynamoJson())

    suspend fun getUserRecords() = documentClient.scan(json("TableName" to tableName)).promise().await()
        .itemsNode()
        .sortByRecordTimestamp()
        .map { it.toUserRecord() }

}

