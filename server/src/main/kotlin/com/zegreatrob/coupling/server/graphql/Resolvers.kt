package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.actionFunc.CommandExecuteSyntax
import com.zegreatrob.coupling.actionFunc.Result
import com.zegreatrob.coupling.actionFunc.SuccessfulResult
import com.zegreatrob.coupling.actionFunc.SuspendResultAction
import com.zegreatrob.coupling.actionFunc.execute
import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.promise
import kotlin.js.Json

fun Json.tribeId() = this["id"].toString()

typealias GraphQLDispatcherProvider<D> = suspend (Request, Json) -> D?

fun <D : CommandExecuteSyntax, Q : SuspendResultAction<D, R>, R, J> dispatch(
    dispatcherFunc: GraphQLDispatcherProvider<D>,
    queryFunc: (Json) -> Q,
    toJson: (R) -> J
) = { entity: Json, _: Json, request: Request ->
    request.scope.promise {
        val command = queryFunc(entity)
        dispatcherFunc(request, entity)
            ?.execute(command)
            ?.successOrNull(toJson)
    }
}

private fun <J, R> Result<R>.successOrNull(toJson: (R) -> J) = when (this) {
    is SuccessfulResult -> toJson(value)
    else -> null
}
