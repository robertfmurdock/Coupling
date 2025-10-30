@file:OptIn(DelicateCoroutinesApi::class)

package com.zegreatrob.coupling.sdk.gql

import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Query
import com.zegreatrob.coupling.sdk.KtorSyntax
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray

interface KtorQueryPerformer :
    QueryPerformer,
    KtorSyntax {

    override suspend fun doQuery(body: JsonElement) = postStringToJsonObject(body)

    override suspend fun doQuery(queryString: String) = postStringToJsonObject(
        JsonObject(mapOf("query" to JsonPrimitive(queryString))),
    )

    override fun postAsync(body: JsonElement) = GlobalScope.async {
        postStringToJsonObject(body)
    }

    private suspend fun postStringToJsonObject(body: JsonElement): JsonElement {
        val result = client.post("/api/graphql") {
            header("Authorization", "Bearer ${getIdToken()}")
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<JsonObject>()
        val errors = result["errors"]
        if (errors != null && errors.jsonArray.isNotEmpty()) {
            throw Error("Failed with errors: $errors. Full body is $result")
        }
        return result
    }

    override suspend fun <D : Mutation.Data> apolloMutation(mutation: Mutation<D>): ApolloResponse<D> = apolloClient
        .mutation(mutation)
        .addHttpHeader("Authorization", "Bearer ${getIdToken()}")
        .execute()

    override suspend fun <D : Query.Data> apolloQuery(query: Query<D>): ApolloResponse<D> = apolloClient
        .query(query)
        .addHttpHeader("Authorization", "Bearer ${getIdToken()}")
        .execute()

    override suspend fun get(path: String): JsonElement = client.get(path) {
        header("Authorization", "Bearer ${getIdToken()}")
    }.body()
}
