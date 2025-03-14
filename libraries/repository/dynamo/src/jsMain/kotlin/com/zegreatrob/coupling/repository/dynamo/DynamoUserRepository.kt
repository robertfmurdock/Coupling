package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.user.UserRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotools.types.text.NotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.js.Json
import kotlin.js.json

class DynamoUserRepository private constructor(override val userId: String, override val clock: Clock) :
    UserRepository,
    UserIdProvider,
    DynamoUserJsonMapping,
    RecordSyntax {

    companion object :
        DynamoDBSyntax by DynamoDbProvider,
        CreateTableParamProvider,
        DynamoItemPutSyntax,
        DynamoQuerySyntax,
        DynamoItemSyntax,
        DynamoScanSyntax {
        override val tableName = "USER"
        const val USER_EMAIL_INDEX = "USER_EMAIL_INDEX"
        private val ensure by lazy {
            MainScope().async { ensureTableExists() }
        }

        suspend operator fun invoke(userId: String, clock: Clock) = DynamoUserRepository(userId, clock)
            .also { ensure.await() }

        override val createTableParams: Json
            get() = json(
                "TableName" to prefixedTableName,
                "KeySchema" to arrayOf(
                    json(
                        "AttributeName" to "id",
                        "KeyType" to "HASH",
                    ),
                    json(
                        "AttributeName" to "timestamp",
                        "KeyType" to "RANGE",
                    ),
                ),
                "AttributeDefinitions" to arrayOf(
                    json(
                        "AttributeName" to "id",
                        "AttributeType" to "S",
                    ),
                    json(
                        "AttributeName" to "timestamp",
                        "AttributeType" to "S",
                    ),
                    json(
                        "AttributeName" to "email",
                        "AttributeType" to "S",
                    ),
                ),
                "BillingMode" to "PAY_PER_REQUEST",
            ).add(
                json(
                    "GlobalSecondaryIndexes" to arrayOf(
                        json(
                            "IndexName" to USER_EMAIL_INDEX,
                            "KeySchema" to arrayOf(
                                json(
                                    "AttributeName" to "email",
                                    "KeyType" to "HASH",
                                ),
                                json(
                                    "AttributeName" to "id",
                                    "KeyType" to "RANGE",
                                ),
                            ),
                            "Projection" to json(
                                "NonKeyAttributes" to arrayOf(
                                    "timestamp",
                                ),
                                "ProjectionType" to "INCLUDE",
                            ),
                        ),
                    ),
                ),
            )
    }

    override suspend fun save(user: UserDetails) = logAsync("saveUser") { saveRawRecord(user.toRecord()) }

    override suspend fun getUser() = logAsync("getUser") { queryAllRecords(queryParams(userId)) }
        .sortByRecordTimestamp()
        .lastOrNull()
        ?.toUserRecord()

    override suspend fun getUsersWithEmail(email: String) = firstEmailIdRecord(email) ?: logAsync("userIdsWithEmail") {
        queryAllRecords(emailQueryParams(email))
            .mapNotNull { it.getDynamoStringValue("id") }
            .distinct()
    }.mapNotNull { userId ->
        logAsync("get user with id latest revision") {
            queryAllRecords(queryParams(userId))
                .sortByRecordTimestamp()
                .lastOrNull()
                ?.toUserRecord()
        }
    }

    private suspend fun firstEmailIdRecord(email: String) = logAsync("firstEmailIdRecord") {
        performQuery(
            json(
                "TableName" to prefixedTableName,
                "ExpressionAttributeValues" to json(":id" to emailId(email)),
                "KeyConditionExpression" to "id = :id",
                "ScanIndexForward" to false,
                "Limit" to 1,
            ),
        )
            .itemsNode()
            .firstOrNull()
            ?.let { it.toRecord(emailIdRecordToUser(it)) }
            ?.let { listOf(it) }
    }

    @OptIn(ExperimentalKotoolsTypesApi::class)
    private fun emailIdRecordToUser(json: Json) = UserDetails(
        json["user_id"].toString(),
        json["email"].toString(),
        json["authorizedTribeIds"]
            .unsafeCast<Array<String?>>()
            .mapNotNull { it?.let(NotBlankString::createOrNull)?.let(::PartyId) }
            .toSet(),
        json.getDynamoStringValue("stripeCustomerId"),
    )

    private fun queryParams(id: String) = json(
        "TableName" to prefixedTableName,
        "ExpressionAttributeValues" to json(":id" to id),
        "KeyConditionExpression" to "id = :id",
    )

    private fun emailQueryParams(email: String) = json(
        "TableName" to prefixedTableName,
        "IndexName" to USER_EMAIL_INDEX,
        "ExpressionAttributeValues" to json(":email" to email),
        "KeyConditionExpression" to "email = :email",
    )

    suspend fun saveRawRecord(record: Record<UserDetails>) = coroutineScope {
        val recordJson = record.recordJson()
        launch { performPutItem(recordJson.add(record.data.asDynamoJson())) }
        launch { performPutItem(recordJson.add(record.asEmailIdDynamoJson())) }
    }.let { }

    private fun Record<UserDetails>.asEmailIdDynamoJson() = json(
        "id" to emailId(data.email),
        "user_id" to data.id,
        "email" to data.email,
        "stripeCustomerId" to data.stripeCustomerId,
        "authorizedTribeIds" to data.authorizedPartyIds.map { it.value.toString() }.toTypedArray(),
    )

    private fun emailId(email: String) = "EMAIL-$email"

    suspend fun getUserRecords() = scanAllRecords()
        .sortByRecordTimestamp()
        .map { it.toUserRecord() }
}
