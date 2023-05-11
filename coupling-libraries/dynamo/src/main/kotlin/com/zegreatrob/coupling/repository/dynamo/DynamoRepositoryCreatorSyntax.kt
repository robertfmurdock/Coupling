package com.zegreatrob.coupling.repository.dynamo

import korlibs.time.TimeProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async

abstract class DynamoRepositoryCreatorSyntax<T> : DynamoCreateTableSyntax {

    abstract val construct: (String, TimeProvider) -> T

    private val ensure by lazy {
        MainScope().async { ensureTableExists() }
    }

    suspend operator fun invoke(userEmail: String, clock: TimeProvider): T = construct(userEmail, clock)
        .also { ensure.await() }
}
