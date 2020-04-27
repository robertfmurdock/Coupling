package com.zegreatrob.coupling.server.entity

import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.CurrentTribeIdDispatcher
import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.promise
import kotlin.js.Json

typealias CommandResolver = suspend CommandDispatcher.(Json, Json) -> Any?

fun verifyAuth(dispatcher: suspend CurrentTribeIdDispatcher.() -> Any?): CommandResolver =
    { entity, _ -> verifyAuth(entity, dispatcher) }

fun buildResolver(func: CommandResolver) = { entity: Json, args: Json, request: Request ->
    request.scope.promise { func(request.commandDispatcher, entity, args) }
}

suspend fun CommandDispatcher.verifyAuth(entity: Json, func: suspend CurrentTribeIdDispatcher.() -> Any?): Any? {
    val dispatcher = authorizedTribeIdDispatcher(entity["id"].toString())
    return when {
        dispatcher.isAuthorized() -> func(dispatcher)
        else -> null
    }
}
