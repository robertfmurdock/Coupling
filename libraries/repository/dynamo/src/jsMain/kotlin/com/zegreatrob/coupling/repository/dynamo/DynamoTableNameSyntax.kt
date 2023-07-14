package com.zegreatrob.coupling.repository.dynamo

interface DynamoTableNameSyntax {
    val tableName: String
    val prefixedTableName: String get() = "${prefix}$tableName"
    private val prefix
        get() = js("process.env.DYNAMO_PREFIX").unsafeCast<String?>() ?: throw Exception("No Dynamo Prefix Configured.")
}
