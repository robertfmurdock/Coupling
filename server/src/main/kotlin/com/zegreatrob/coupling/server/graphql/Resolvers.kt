package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.action.SuspendAction
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
            loggedExecute(request, queryFunc(entity), toJson, it)
        }
    }
}
