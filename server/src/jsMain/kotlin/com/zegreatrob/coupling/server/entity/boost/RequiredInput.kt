package com.zegreatrob.coupling.server.entity.boost

import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.graphql.CommandFunc
import com.zegreatrob.coupling.server.graphql.GraphQLDispatcherProvider
import kotlinx.serialization.json.JsonNull

inline fun <reified E, reified I, reified C> requiredInput(crossinline callback: (_: E, input: I) -> C?): CommandFunc<E, I, C> = { e, i ->
    i?.let { callback(e, i) }
}

inline fun <reified E, reified I, reified D> requiredInput(crossinline callback: suspend (CouplingContext, E, I) -> D?): GraphQLDispatcherProvider<E, I?, D> = { couplingContext, e, i ->
    i?.let { callback(couplingContext, e, i) }
}

inline fun <reified E, reified D> adapt(crossinline callback: suspend (CouplingContext) -> D?): GraphQLDispatcherProvider<E, JsonNull?, D> = { couplingContext, _, _ -> callback(couplingContext) }
