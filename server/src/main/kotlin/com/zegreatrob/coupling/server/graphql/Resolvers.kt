package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.action.*
import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.promise
import kotlin.js.Json

fun Json.tribeId() = this["id"].toString()

typealias GraphQLDispatcherProvider<D> = suspend (Request, Json) -> D?

fun <D : ActionLoggingSyntax, Q : SuspendAction<D, R>, R, J> dispatch(
    dispatcherFunc: GraphQLDispatcherProvider<D>,
    queryFunc: (Json) -> Q,
    toJson: (R) -> J
) = { entity: Json, _: Json, request: Request ->
    request.scope.promise {
        val command = queryFunc(entity)
        val dispatcher = dispatcherFunc(request, entity)

        with(object : CommandExecuteSyntax {}) {
            dispatcher?.execute(command)
        }?.let { result -> successOrNull(result, toJson) }
    }
}

private fun <J, R> successOrNull(result: Result<R>, toJson: (R) -> J) = when (result) {
    is SuccessfulResult -> toJson(result.value)
    else -> null
}
