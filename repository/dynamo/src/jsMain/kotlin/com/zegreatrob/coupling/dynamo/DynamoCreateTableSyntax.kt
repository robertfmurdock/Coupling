package com.zegreatrob.coupling.dynamo

import kotlinx.coroutines.await
import kotlinx.coroutines.yield
import kotlin.js.Json
import kotlin.js.json


val validatedTableList = mutableListOf<String>()

interface DynamoCreateTableSyntax : DynamoTableNameSyntax, DynamoDBSyntax {
    val createTableParams: Json

    suspend fun ensureTableExists() {
        if (validatedTableList.contains(tableName)) {
            return
        } else {
            if (!checkTableExists()) {
                createTable()

                while (tableStatus() != "ACTIVE") {
                    yield()
                }
            }
            validatedTableList.add(tableName)
        }
    }

    suspend fun checkTableExists() = try {
        describeTribeTable()
        true
    } catch (throwable: Throwable) {
        false
    }

    suspend fun createTable() = dynamoDB.createTable(createTableParams).promise().await()

    suspend fun tableStatus() = describeTribeTable()
        .let { it["Table"].unsafeCast<Json>()["TableStatus"] }

    private suspend fun describeTribeTable(): Json =
        dynamoDB.describeTable(json("TableName" to tableName))
            .promise()
            .await()

}