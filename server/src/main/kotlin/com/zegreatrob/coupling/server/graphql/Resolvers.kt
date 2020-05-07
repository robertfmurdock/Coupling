package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.server.express.route.loggedExecute
import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.promise
import kotlin.js.Json

fun Json.tribeId() = this["id"].toString()

typealias GraphQLDispatcherProvider<D> = suspend (Request, Json) -> D?

fun <D, Q : SuspendAction<D, R>, R, J> dispatch(
    dispatcher: GraphQLDispatcherProvider<D>,
    queryFunc: (Json) -> Q,
    toJson: (R) -> J
) = { entity: Json, _: Json, request: Request ->
    request.scope.promise {
        dispatcher(request, entity)?.let {
            request.commandDispatcher.loggedExecute(it, queryFunc(entity))
                .let { result -> successOrNull(result, toJson) }
        }
    }
}

private fun <J, R> successOrNull(result: Result<R>, toJson: (R) -> J) = when (result) {
    is SuccessfulResult -> toJson(result.value)
    else -> null
}
