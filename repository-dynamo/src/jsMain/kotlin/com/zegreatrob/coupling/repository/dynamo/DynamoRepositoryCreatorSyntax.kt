package com.zegreatrob.coupling.repository.dynamo

import com.soywiz.klock.TimeProvider

interface DynamoRepositoryCreatorSyntax<T> : DynamoCreateTableSyntax {

    val construct: (String, TimeProvider) -> T

    suspend operator fun invoke(userEmail: String, clock: TimeProvider): T = construct(userEmail, clock)
        .also { ensureTableExists() }

}