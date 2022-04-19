package com.zegreatrob.coupling.repository.dynamo

import kotlin.js.json

interface CreateTableParamProvider :
    com.zegreatrob.coupling.repository.dynamo.DynamoCreateTableSyntax,
    com.zegreatrob.coupling.repository.dynamo.DynamoTableNameSyntax {
    override val createTableParams
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
                )
            ),
            "BillingMode" to "PAY_PER_REQUEST"
        )
}
