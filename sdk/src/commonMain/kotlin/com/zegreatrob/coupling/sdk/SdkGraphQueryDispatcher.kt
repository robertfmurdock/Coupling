package com.zegreatrob.coupling.sdk

import com.apollographql.apollo.api.Query
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkGraphQueryDispatcher :
    ApolloGraphQuery.Dispatcher,
    GqlTrait {

    override suspend fun <D : Query.Data> perform(query: ApolloGraphQuery<D>): D = performer.apolloQuery(query.query)
        .dataAssertNoErrors
}
