package com.zegreatrob.coupling.sdk

import kotlinx.coroutines.await
import kotlin.js.json

interface GqlSyntax : AxiosSyntax {
    suspend fun String.performQuery() = axios.post("/api/graphql", json("query" to this)).await()
}
