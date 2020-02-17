package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.user.UserRepository
import kotlinx.coroutines.await
import kotlin.js.json

class DynamoUserRepository private constructor(override val userEmail: String) : UserRepository, UserEmailSyntax {

    companion object : DynamoDBSyntax by DynamoDbProvider,
        DynamoCreateTableSyntax,
        DynamoItemPutSyntax,
        DynamoQuerySyntax,
        DynamoItemSyntax,
        DynamoUserJsonMapping {

        suspend operator fun invoke(email: String) = DynamoUserRepository(email).also { ensureTableExists() }

        override val tableName = "USER"
        override val createTableParams = json(
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
                )
            ),
            "BillingMode" to "PAY_PER_REQUEST"
        )

    }

    override suspend fun save(user: User) = performPutItem(user.asDynamoJson())

    override suspend fun getUser(): User? = documentClient.scan(emailQuery()).promise().await()
        .itemsNode()
        .sortByRecordTimestamp()
        .lastOrNull()
        ?.toUser()

    private fun emailQuery() = json(
        "TableName" to tableName,
        "ExpressionAttributeValues" to json(":email" to userEmail),
        "FilterExpression" to "email = :email"
    )

}
