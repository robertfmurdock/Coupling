package com.zegreatrob.coupling.sdk.gql

import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Mutation
import kotlinx.coroutines.Deferred
import kotlinx.serialization.json.JsonElement

interface QueryPerformer {
    suspend fun doQuery(queryString: String): JsonElement
    suspend fun doQuery(body: JsonElement): JsonElement
    fun postAsync(body: JsonElement): Deferred<JsonElement>

    suspend fun get(path: String): JsonElement
    suspend fun <D : Mutation.Data> apolloMutation(mutation: Mutation<D>): ApolloResponse<D>
}
