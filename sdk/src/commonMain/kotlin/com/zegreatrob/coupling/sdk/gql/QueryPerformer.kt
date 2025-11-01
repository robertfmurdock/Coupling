package com.zegreatrob.coupling.sdk.gql

import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Query

interface QueryPerformer {
    suspend fun <D : Mutation.Data> apolloMutation(mutation: Mutation<D>): ApolloResponse<D>
    suspend fun <D : Query.Data> apolloQuery(query: Query<D>): ApolloResponse<D>
}
