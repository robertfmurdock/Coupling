@file:OptIn(DelicateCoroutinesApi::class)

package com.zegreatrob.coupling.sdk.gql

import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Query
import com.zegreatrob.coupling.sdk.KtorSyntax
import kotlinx.coroutines.DelicateCoroutinesApi

interface KtorQueryPerformer :
    QueryPerformer,
    KtorSyntax {

    override suspend fun <D : Mutation.Data> apolloMutation(mutation: Mutation<D>): ApolloResponse<D> = apolloClient
        .mutation(mutation)
        .addHttpHeader("Authorization", "Bearer ${getIdToken()}")
        .execute()

    override suspend fun <D : Query.Data> apolloQuery(query: Query<D>): ApolloResponse<D> = apolloClient
        .query(query)
        .addHttpHeader("Authorization", "Bearer ${getIdToken()}")
        .execute()
}
