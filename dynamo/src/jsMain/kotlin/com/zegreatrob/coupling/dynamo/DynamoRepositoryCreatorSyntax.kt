package com.zegreatrob.coupling.dynamo

interface DynamoRepositoryCreatorSyntax<T> : DynamoCreateTableSyntax {

    val construct: (String) -> T

    suspend operator fun invoke(userEmail: String): T = construct(userEmail).also { ensureTableExists() }

}