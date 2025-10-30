package com.zegreatrob.coupling.sdk.gql

import com.apollographql.apollo.api.Query
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class ApolloGraphQuery<D : Query.Data>(val query: Query<D>) : SimpleSuspendAction<ApolloGraphQuery.Dispatcher, D?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun <D : Query.Data> perform(query: ApolloGraphQuery<D>): D
    }
}
