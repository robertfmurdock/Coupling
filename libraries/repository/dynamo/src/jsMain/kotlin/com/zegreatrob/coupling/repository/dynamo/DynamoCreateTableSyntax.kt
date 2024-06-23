package com.zegreatrob.coupling.repository.dynamo

import kotlinx.coroutines.await
import kotlinx.coroutines.yield
import kotlin.js.Json
import kotlin.js.json

val validatedTableList = mutableListOf<String>()

interface DynamoCreateTableSyntax :
    DynamoTableNameSyntax,
    DynamoDBSyntax,
    DynamoLoggingSyntax {
    val createTableParams: Json
    suspend fun ensureTableExists() {
        if (!validatedTableList.contains(prefixedTableName)) {
            logAsync("ensureTableExists $prefixedTableName") {
                if (!checkTableExists()) {
                    createTable()
                    waitForActive()
                }
                validatedTableList.add(prefixedTableName)
            }
        }
    }

    val waitForActiveLogHeader get() = "waitForActive $prefixedTableName"
    private suspend fun waitForActive() = logAsync(waitForActiveLogHeader) {
        while (tableStatus() != "ACTIVE") {
            yield()
        }
    }

    val checkTableExistsLogHeader get() = "checkTableExists $prefixedTableName"
    suspend fun checkTableExists() = logAsync(checkTableExistsLogHeader) {
        try {
            describeTribeTable()
            true
        } catch (throwable: Throwable) {
            false
        }
    }

    val createTableLogHeader get() = "create table $prefixedTableName"
    suspend fun createTable(): dynamic = logAsync(createTableLogHeader) {
        dynamoDB.createTable(createTableParams).await()
    }

    suspend fun tableStatus() = describeTribeTable()
        .let { it["Table"].unsafeCast<Json>()["TableStatus"] }

    private suspend fun describeTribeTable(): Json = dynamoDB.describeTable(json("TableName" to prefixedTableName))
        .await()
}
