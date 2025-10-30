package com.zegreatrob.coupling.sdk

import com.apollographql.apollo.api.Query
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.RawGraphQuery
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface SdkGraphQueryDispatcher :
    RawGraphQuery.Dispatcher,
    ApolloGraphQuery.Dispatcher,
    GqlTrait {

    override suspend fun perform(query: RawGraphQuery) = query.postBody().perform()

    private fun RawGraphQuery.postBody() = buildJsonObject {
        put("query", queryString)
        variables?.let { put("variables", it) }
    }

    override suspend fun <D : Query.Data> perform(query: ApolloGraphQuery<D>): D = performer.apolloQuery(query.query).dataAssertNoErrors
}
