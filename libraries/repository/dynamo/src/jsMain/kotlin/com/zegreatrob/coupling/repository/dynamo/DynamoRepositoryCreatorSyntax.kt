package com.zegreatrob.coupling.repository.dynamo

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.datetime.Clock

abstract class DynamoRepositoryCreatorSyntax<T> : DynamoCreateTableSyntax {

    abstract val construct: (String, Clock) -> T

    private val ensure by lazy {
        MainScope().async { ensureTableExists() }
    }

    suspend operator fun invoke(userEmail: String, clock: Clock): T = construct(userEmail, clock)
        .also { ensure.await() }
}
