package com.zegreatrob.coupling.repository.dynamo

import kotlin.js.json

interface PartyCreateTableParamProvider : DynamoCreateTableSyntax, DynamoTableNameSyntax {
    override val createTableParams
        get() = json(
            "TableName" to prefixedTableName,
            "KeySchema" to arrayOf(
                json(
                    "AttributeName" to "tribeId",
                    "KeyType" to "HASH",
                ),
                json(
                    "AttributeName" to "timestamp+id",
                    "KeyType" to "RANGE",
                ),
            ),
            "AttributeDefinitions" to arrayOf(
                json(
                    "AttributeName" to "tribeId",
                    "AttributeType" to "S",
                ),
                json(
                    "AttributeName" to "timestamp+id",
                    "AttributeType" to "S",
                ),
            ),
            "BillingMode" to "PAY_PER_REQUEST",
        )
}
