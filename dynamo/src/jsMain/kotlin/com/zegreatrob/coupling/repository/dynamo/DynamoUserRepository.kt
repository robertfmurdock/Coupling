package com.zegreatrob.coupling.repository.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserIdSyntax
import com.zegreatrob.coupling.repository.user.UserRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlin.js.Json
import kotlin.js.json

class DynamoUserRepository private constructor(override val userId: String, override val clock: TimeProvider) :
    UserRepository,
    UserIdSyntax,
    DynamoUserJsonMapping,
    RecordSyntax {

    companion object :
        DynamoDBSyntax by DynamoDbProvider,
        com.zegreatrob.coupling.repository.dynamo.CreateTableParamProvider,
        DynamoItemPutSyntax,
        DynamoQuerySyntax,
        DynamoItemSyntax,
        DynamoScanSyntax {
        override val tableName = "USER"
        const val userEmailIndex = "USER_EMAIL_INDEX"
        private val ensure by lazy {
            MainScope().async { ensureTableExists() }
        }
        suspend operator fun invoke(userId: String, clock: TimeProvider) = DynamoUserRepository(userId, clock)
            .also { ensure.await() }

        override val createTableParams: Json
            get() = json(
                "TableName" to prefixedTableName,
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

    override suspend fun save(user: User) = logAsync("saveUser") { performPutItem(user.toRecord().asDynamoJson()) }

    override suspend fun getUser() = logAsync("getUser") { performQuery(queryParams(userId)) }
        .itemsNode()
        .sortByRecordTimestamp()
        .lastOrNull()
        ?.toUserRecord()

    override suspend fun getUsersWithEmail(email: String) = logAsync("userIdsWithEmail") {
        performQuery(emailQueryParams(email))
            .itemsNode()
            .mapNotNull { it.getDynamoStringValue("id") }
            .distinct()
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
        "TableName" to prefixedTableName,
        "ExpressionAttributeValues" to json(":id" to id),
        "KeyConditionExpression" to "id = :id"
    )

    private fun emailQueryParams(email: String) = json(
        "TableName" to prefixedTableName,
        "IndexName" to userEmailIndex,
        "ExpressionAttributeValues" to json(":email" to email),
        "KeyConditionExpression" to "email = :email"
    )

    suspend fun saveRawRecord(record: Record<User>) = performPutItem(record.asDynamoJson())

    suspend fun getUserRecords() = scanAllRecords()
        .sortByRecordTimestamp()
        .map { it.toUserRecord() }
}
