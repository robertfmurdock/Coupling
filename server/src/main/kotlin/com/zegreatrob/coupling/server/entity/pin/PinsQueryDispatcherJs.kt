package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.toJsonArray
import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.action.pin.PinsQuery
import com.zegreatrob.coupling.server.action.pin.PinsQueryDispatcher
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedActionDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.Promise

interface PinsQueryDispatcherJs : PinsQueryDispatcher, ScopeSyntax, UserIsAuthorizedActionDispatcher {

    suspend fun performPinListQueryGQL() = PinsQuery
        .perform()
        ?.toJsonArray()

}

@Suppress("unused")
@JsName("pinListResolver")
val pinListResolver: GraphQLResolver
    get() = buildResolver { commandDispatcher, entity, _ ->
        val authorizedDispatcher = commandDispatcher
            .authorizedTribeIdDispatcher(entity["id"].toString())
        authorizedDispatcher.performPinListQueryGQL()
    }

private fun buildResolver(func: suspend (CommandDispatcher, Json, Json) -> Array<Json>?): (Json, Json, Request) -> Promise<Array<Json>?> =
    { entity, args, request ->
        val commandDispatcher = request.commandDispatcher.unsafeCast<CommandDispatcher>()
        commandDispatcher.scope.promise {
            func(commandDispatcher, entity, args)
        }
    }

typealias GraphQLResolver = (Json, Json, Request) -> Promise<Any?>