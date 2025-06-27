package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.user.UserRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotools.types.text.NotBlankString
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.js.Json
import kotlin.js.json
import kotlin.time.Clock

class DynamoUserRepository private constructor(override val userId: UserId, override val clock: Clock) :
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

        suspend operator fun invoke(userId: UserId, clock: Clock) = DynamoUserRepository(userId, clock)
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

    override suspend fun getUser() = logAsync("getUser") { queryAllRecords(queryParams(userId.value.toString())) }
        .sortByRecordTimestamp()
        .lastOrNull()
        ?.toUserRecord()

    override suspend fun getUsersWithEmail(email: NotBlankString) = firstEmailIdRecord(email) ?: logAsync("userIdsWithEmail") {
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

    private suspend fun firstEmailIdRecord(email: NotBlankString) = logAsync("firstEmailIdRecord") {
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
        UserId(json["user_id"].toString().toNotBlankString().getOrThrow()),
        json["email"].toString().toNotBlankString().getOrThrow(),
        json["authorizedTribeIds"]
            .unsafeCast<Array<String?>>()
            .mapNotNull { it?.let(::PartyId) }
            .toSet(),
        json.getDynamoStringValue("stripeCustomerId"),
    )

    private fun queryParams(id: String) = json(
        "TableName" to prefixedTableName,
        "ExpressionAttributeValues" to json(":id" to id),
        "KeyConditionExpression" to "id = :id",
    )

    private fun emailQueryParams(email: NotBlankString) = json(
        "TableName" to prefixedTableName,
        "IndexName" to USER_EMAIL_INDEX,
        "ExpressionAttributeValues" to json(":email" to email.toString()),
        "KeyConditionExpression" to "email = :email",
    )

    suspend fun saveRawRecord(record: Record<UserDetails>) = coroutineScope {
        val recordJson = record.recordJson()
        launch { performPutItem(recordJson.add(record.data.asDynamoJson())) }
        launch { performPutItem(recordJson.add(record.asEmailIdDynamoJson())) }
    }.let { }

    private fun Record<UserDetails>.asEmailIdDynamoJson() = json(
        "id" to emailId(data.email),
        "user_id" to data.id.value.toString(),
        "email" to data.email.toString(),
        "stripeCustomerId" to data.stripeCustomerId,
        "authorizedTribeIds" to data.authorizedPartyIds.map { it.value.toString() }.toTypedArray(),
    )

    private fun emailId(email: NotBlankString) = "EMAIL-$email"

    suspend fun getUserRecords() = scanAllRecords()
        .sortByRecordTimestamp()
        .map { it.toUserRecord() }
}
