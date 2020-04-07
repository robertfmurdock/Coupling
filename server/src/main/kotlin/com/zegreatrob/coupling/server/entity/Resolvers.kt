package com.zegreatrob.coupling.server.entity

import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.CurrentTribeIdDispatcher
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

    @JsName("tribe")
    val tribe = buildResolver { entity, _ ->
        performTribeQueryGQL(entity["id"].toString())
    }

    @JsName("tribeList")
    val tribeList = buildResolver { _, _ ->
        performTribeListQueryGQL()
    }
}

private fun buildResolver(func: suspend CommandDispatcher.(Json, Json) -> Any?): (Json, Json, Request) -> Promise<Any?> =
    { entity, args, request ->
        val commandDispatcher = request.commandDispatcher.unsafeCast<CommandDispatcher>()
        commandDispatcher.scope.promise {
            func(commandDispatcher, entity, args)
        }
    }

private fun buildAuthorizedResolver(func: suspend CurrentTribeIdDispatcher.() -> Any?) =
    buildResolver { entity, _ ->
        val authorizedDispatcher = authorizedTribeIdDispatcher(entity["id"].toString())
        authorizedDispatcher.func()
            .let {
                when {
                    authorizedDispatcher.isAuthorized() -> it
                    else -> null
                }
            }
    }

typealias GraphQLResolver = (Json, Json, Request) -> Promise<Any?>