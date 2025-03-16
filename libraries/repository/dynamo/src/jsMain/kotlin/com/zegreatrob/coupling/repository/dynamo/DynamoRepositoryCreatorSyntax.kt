package com.zegreatrob.coupling.repository.dynamo

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.datetime.Clock
import kotools.types.text.NotBlankString

abstract class DynamoRepositoryCreatorSyntax<T> : DynamoCreateTableSyntax {

    abstract val construct: (NotBlankString, Clock) -> T

    private val ensure by lazy {
        MainScope().async { ensureTableExists() }
    }

    suspend operator fun invoke(userEmail: NotBlankString, clock: Clock): T = construct(userEmail, clock)
        .also { ensure.await() }
}
