package com.zegreatrob.coupling.server.entity

import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.CurrentTribeIdDispatcher
import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.promise
import kotlin.js.Json

typealias CommandResolver = suspend CommandDispatcher.(Json, Json) -> Any?

fun verifyAuth(dispatcher: suspend CurrentTribeIdDispatcher.() -> Any?): CommandResolver = { entity, _ ->
    verifyAuth(entity, dispatcher)
}

fun buildResolver(func: CommandResolver) = { entity: Json, args: Json, request: Request ->
    request.scope.promise { func(request.commandDispatcher, entity, args) }
}

suspend fun CommandDispatcher.verifyAuth(entity: Json, func: suspend CurrentTribeIdDispatcher.() -> Any?): Any? {
    val dispatcher = authorizedTribeIdDispatcher(entity.tribeId())
    return when {
        dispatcher.isAuthorized() -> func(dispatcher)
        else -> null
    }
}

private fun Json.tribeId() = this["id"].toString()

fun <Q, R> dispatchTribeCommand(
    toQuery: () -> Q,
    dispatch: suspend CurrentTribeIdDispatcher.(Q) -> R,
    toJson: (R) -> Any?
): CommandResolver = { entity, more ->
    val query = toQuery()

    verifyAuth {
        val result = dispatch(query)
        toJson(result)
    }(entity, more)
}