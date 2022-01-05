package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.user.UserRepository
import kotlin.js.Json
import kotlin.js.json

class DynamoUserRepository private constructor(override val userId: String, override val clock: TimeProvider) :
    UserRepository,
    UserEmailSyntax,
    DynamoUserJsonMapping,
    RecordSyntax {

    companion object : DynamoDBSyntax by DynamoDbProvider,
        CreateTableParamProvider,
        DynamoItemPutSyntax,
        DynamoQuerySyntax,
        DynamoItemSyntax,
        DynamoScanSyntax {
        override val tableName = "USER"
        const val userEmailIndex = "USER_EMAIL_INDEX"
        suspend operator fun invoke(userId: String, clock: TimeProvider) = DynamoUserRepository(userId, clock)
            .also { ensureTableExists() }

        override val createTableParams: Json
            get() = json(
                "TableName" to tableName,
                "KeySchema" to arrayOf(
                    json(
                        "AttributeName" to "id",
                        "KeyType" to "HASH"
                    ),
                    json(
                        "AttributeName" to "timestamp",
                        "KeyType" to "RANGE"
                    )
                ),
                "AttributeDefinitions" to arrayOf(
                    json(
                        "AttributeName" to "id",
                        "AttributeType" to "S"
                    ),
                    json(
                        "AttributeName" to "timestamp",
                        "AttributeType" to "S"
                    ),
                    json(
                        "AttributeName" to "email",
                        "AttributeType" to "S"
                    )
                ),
                "BillingMode" to "PAY_PER_REQUEST"
            ).add(
                json(
                    "GlobalSecondaryIndexes" to arrayOf(
                        json(
                            "IndexName" to userEmailIndex,
                            "KeySchema" to arrayOf(
                                json(
                                    "AttributeName" to "email",
                                    "KeyType" to "HASH"
                                ),
                                json(
                                    "AttributeName" to "id",
                                    "KeyType" to "RANGE"
                                )
                            ),
                            "Projection" to json(
                                "NonKeyAttributes" to arrayOf(
                                    "timestamp"
                                ),
                                "ProjectionType" to "INCLUDE"
                            )
                        )
                    )
                )
            )

    }

    override suspend fun save(user: User) = performPutItem(user.toRecord().asDynamoJson())

    override suspend fun getUser() = logAsync("getUser") { performQuery(queryParams(userId)) }
            .itemsNode()
            .sortByRecordTimestamp()
            .lastOrNull()
            ?.toUserRecord()

    override suspend fun getUsersWithEmail(email: String) = logAsync("userIdsWithEmail") {
        performQuery(emailQueryParams(email))
            .itemsNode()
            .mapNotNull { it.getDynamoStringValue("id") }
    }.mapNotNull { userId ->
        logAsync("get user with id latest revision") {
            performQuery(queryParams(userId))
                .itemsNode()
                .sortByRecordTimestamp()
                .lastOrNull()
                ?.toUserRecord()
        }
    }

    private fun queryParams(id: String) = json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(":id" to id),
        "KeyConditionExpression" to "id = :id"
    )

    private fun emailQueryParams(email: String) = json(
        "TableName" to tableName,
        "IndexName" to userEmailIndex,
        "ExpressionAttributeValues" to json(":email" to email),
        "KeyConditionExpression" to "email = :email"
    )

    suspend fun saveRawRecord(record: Record<User>) = performPutItem(record.asDynamoJson())

    suspend fun getUserRecords() = scanAllRecords()
        .sortByRecordTimestamp()
        .map { it.toUserRecord() }

}
