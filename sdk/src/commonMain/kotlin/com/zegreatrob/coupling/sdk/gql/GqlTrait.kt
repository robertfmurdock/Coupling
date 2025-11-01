package com.zegreatrob.coupling.sdk.gql

import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Mutation

interface GqlTrait {
    val performer: QueryPerformer

    suspend fun <D : Mutation.Data> Mutation<D>.execute(): ApolloResponse<D> = performer.apolloMutation(this)
}
