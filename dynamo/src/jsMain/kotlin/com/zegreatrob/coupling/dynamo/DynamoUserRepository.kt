package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.user.UserRepository
import kotlinx.coroutines.await
import kotlin.js.json

class DynamoUserRepository private constructor(override val userEmail: String) : UserRepository, UserEmailSyntax {

    companion object : DynamoDBSyntax by DynamoDbProvider,
        CreateTableParamProvider,
        DynamoItemPutSyntax,
        DynamoQuerySyntax,
        DynamoItemSyntax,
        DynamoUserJsonMapping {
        override val tableName = "USER"
        suspend operator fun invoke(email: String) = DynamoUserRepository(email).also { ensureTableExists() }
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
