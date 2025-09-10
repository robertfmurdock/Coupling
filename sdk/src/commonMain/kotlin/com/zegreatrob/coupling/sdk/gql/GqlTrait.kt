package com.zegreatrob.coupling.sdk.gql

import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Mutation
import com.zegreatrob.coupling.json.GqlQuery
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toDomain
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

interface GqlTrait {
    val performer: QueryPerformer

    suspend fun String.performQuery(): JsonElement = performer.doQuery(this)
    suspend fun <D : Mutation.Data> Mutation<D>.execute(): ApolloResponse<D> = performer.apolloMutation(this)

    suspend fun performQuery(body: JsonElement): JsonElement = performer.doQuery(body)
    suspend fun JsonElement.perform() = performQuery(this)
        .let { response ->
            response.jsonObject["data"]
                ?.let<JsonElement, GqlQuery>(couplingJsonFormat::decodeFromJsonElement)
                ?.toDomain(response)
        }

    suspend fun String.perform() = performQuery().let { jsonResult ->
        jsonResult.jsonObject["data"]?.let<JsonElement, GqlQuery>(couplingJsonFormat::decodeFromJsonElement)
            ?.toDomain(jsonResult)
    }
}
