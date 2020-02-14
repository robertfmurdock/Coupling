package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserRepository
import kotlin.js.json

class DynamoUserRepository : UserRepository {

    companion object : DynamoRepositoryCreatorSyntax<DynamoUserRepository>, DynamoDBSyntax by DynamoDbProvider,
        DynamoCreateTableSyntax,
        DynamoItemPutSyntax {
        override val construct = ::DynamoUserRepository
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

    override suspend fun save(user: User) {}

    override suspend fun getUser(): User? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
