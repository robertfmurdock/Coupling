package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.CurrentTribeIdDispatcher
import com.zegreatrob.coupling.server.action.SuspendAction
import com.zegreatrob.coupling.server.express.route.loggedExecute
import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.promise
import kotlin.js.Json

typealias CommandResolver = suspend CommandDispatcher.(Json, Json) -> Any?

fun buildResolver(func: CommandResolver) = { entity: Json, args: Json, request: Request ->
    request.scope.promise { func(request.commandDispatcher, entity, args) }
}

suspend fun <R : Any?> CommandDispatcher.verifyAuth(entity: Json, func: suspend CurrentTribeIdDispatcher.() -> R): R? {
    val dispatcher = authorizedTribeIdDispatcher(entity.tribeId())
    return when {
        dispatcher.isAuthorized() -> func(dispatcher)
        else -> null
    }
}

fun Json.tribeId() = this["id"].toString()

fun <Q, R, J> dispatchTribeCommand(
    toQuery: () -> Q,
    dispatch: suspend CurrentTribeIdDispatcher.(Q) -> R,
    toJson: (R) -> J
): CommandResolver = { entity, _ ->
    verifyAuth(entity) { dispatch(toQuery()) }
        ?.let(toJson)
}

fun <Q, R, J> dispatchCommand(
    toQuery: (Json) -> Q,
    dispatch: suspend CommandDispatcher.(Q) -> R,
    toJson: (R) -> J
): CommandResolver = { entity, _ -> toJson(dispatch(toQuery(entity))) }

fun <D, Q : SuspendAction<D, R>, R, J> dispatchCommand(
    dispatcher: suspend (Request, Json) -> D?,
    queryFunc: (Json) -> Q,
    toJson: (R) -> J
) = { entity: Json, _: Json, request: Request ->
    request.scope.promise {
        dispatcher(request, entity)
            ?.let {
                loggedExecute(request, queryFunc(entity), toJson, it)
            }
    }
}

suspend fun commandDispatcher(request: Request, entity: Json) = request.commandDispatcher

suspend fun tribeCommandDispatcher(request: Request, entity: Json) =
    request.commandDispatcher.authorizedTribeIdDispatcher(entity.tribeId())
        .let { if (it.isAuthorized()) it else null }