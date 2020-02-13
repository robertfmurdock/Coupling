package com.zegreatrob.coupling.dynamo

interface DynamoRepositoryCreatorSyntax<T> : DynamoCreateTableSyntax {

    val construct: () -> T

    suspend operator fun invoke(): T = construct().also { ensureTableExists() }

}