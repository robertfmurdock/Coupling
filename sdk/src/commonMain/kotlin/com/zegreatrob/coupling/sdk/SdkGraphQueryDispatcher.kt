package com.zegreatrob.coupling.sdk

import com.apollographql.apollo.api.Query
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkGraphQueryDispatcher :
    GqlQuery.Dispatcher,
    GqlTrait {

    override suspend fun <D : Query.Data> perform(query: GqlQuery<D>): D = performer.apolloQuery(query.query)
        .dataAssertNoErrors
}
