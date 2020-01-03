package com.zegreatrob.coupling.server.entity

import com.zegreatrob.coupling.server.AuthorizedTribeIdDispatcher
import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.Promise

@Suppress("unused")
@JsName("Resolvers")
object Resolvers {
    @JsName("pinList")
    val pinList: GraphQLResolver = buildAuthorizedResolver { performPinListQueryGQL() }

    @JsName("playerList")
    val playerList: GraphQLResolver = buildAuthorizedResolver { performPlayerListQueryGQL() }

    @JsName("pairAssignmentDocumentList")
    val pairAssignmentDocumentList = buildAuthorizedResolver { performPairAssignmentDocumentListQueryGQL() }
}

private fun buildResolver(func: suspend (CommandDispatcher, Json, Json) -> Array<Json>?): (Json, Json, Request) -> Promise<Array<Json>?> =
    { entity, args, request ->
        val commandDispatcher = request.commandDispatcher.unsafeCast<CommandDispatcher>()
        commandDispatcher.scope.promise {
            func(commandDispatcher, entity, args)
        }
    }

private fun buildAuthorizedResolver(func: suspend AuthorizedTribeIdDispatcher.() -> Array<Json>?): (Json, Json, Request) -> Promise<Array<Json>?> {
    return buildResolver { commandDispatcher, entity, _ ->
        val authorizedDispatcher = commandDispatcher
            .authorizedTribeIdDispatcher(entity["id"].toString())
        authorizedDispatcher.func()
    }
}

typealias GraphQLResolver = (Json, Json, Request) -> Promise<Any?>