package com.zegreatrob.coupling.sdk

import kotlinx.coroutines.Deferred
import kotlin.js.Json

interface KtorQueryPerformer : QueryPerformer, KtorSyntax {

    override suspend fun doQuery(body: Json): dynamic {
        TODO("Not yet implemented")
    }

    override suspend fun doQuery(body: String): dynamic {
        TODO("Not yet implemented")
    }

    override fun <T> postAsync(body: dynamic): Deferred<T> {
        TODO("Not yet implemented")
    }
}