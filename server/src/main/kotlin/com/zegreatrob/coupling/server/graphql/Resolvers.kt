package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.action.SuspendAction
import com.zegreatrob.coupling.server.express.route.loggedExecute
import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.promise
import kotlin.js.Json

fun Json.tribeId() = this["id"].toString()

fun <D, Q : SuspendAction<D, R>, R, J> dispatchCommand(
    dispatcher: suspend (Request, Json) -> D?,
    queryFunc: (Json) -> Q,
    toJson: (R) -> J
) = { entity: Json, _: Json, request: Request ->
    request.scope.promise {
        dispatcher(request, entity)?.let {
            loggedExecute(request, queryFunc(entity), toJson, it)
        }
    }
}

@Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER")
suspend fun commandDispatcher(request: Request, entity: Json) = request.commandDispatcher

suspend fun tribeCommandDispatcher(request: Request, entity: Json) =
    request.commandDispatcher.authorizedTribeIdDispatcher(entity.tribeId())
        .let { if (it.isAuthorized()) it else null }
