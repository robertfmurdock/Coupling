package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.action.SuspendResultAction
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax
import com.zegreatrob.testmints.action.async.execute
import kotlinx.coroutines.promise
import kotlin.js.Json

typealias GraphQLDispatcherProvider<D> = suspend (Request, Json?, Json?) -> D?

fun <D : SuspendActionExecuteSyntax, Q : SuspendResultAction<D, R>, R, J> dispatch(
    dispatcherFunc: GraphQLDispatcherProvider<D>,
    queryFunc: (Json, Json) -> Q,
    toJson: (R) -> J
) = { entity: Json, args: Json, request: Request ->
    request.scope.promise {
        val command = queryFunc(entity, args)
        dispatcherFunc(request, entity, args)
            ?.execute(command)
            ?.successOrNull(toJson)
    }
}

private fun <J, R> Result<R>.successOrNull(toJson: (R) -> J) = when (this) {
        is SuccessfulResult -> toJson(value)
        else -> null
    }
