package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.action.SuspendResultAction
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.minjson.at
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax
import com.zegreatrob.testmints.action.async.execute
import kotlinx.coroutines.promise
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Json

typealias GraphQLDispatcherProvider<D> = suspend (Request, Json?, Any?) -> D?

@OptIn(ExperimentalSerializationApi::class)
inline fun <D : SuspendActionExecuteSyntax, Q : SuspendResultAction<D, R>, reified R, reified J, reified I> dispatch(
    crossinline dispatcherFunc: GraphQLDispatcherProvider<D>,
    crossinline queryFunc: (Json, I) -> Q,
    crossinline toSerializable: (R) -> J
) = { entity: Json, args: Json, request: Request, info: Json ->

    println("graphql entity ${JSON.stringify(entity)}, args ${JSON.stringify(args)}, info ${JSON.stringify(info)}")

    request.scope.promise {
        val input = couplingJsonFormat.decodeFromDynamic<I>(args.at("/input"))
        val command = queryFunc(entity, input)
        dispatcherFunc(request, entity, input)
            ?.execute(command)
            ?.successOrNull { couplingJsonFormat.encodeToDynamic(toSerializable(it)) }
    }
}

inline fun <J, R> Result<R>.successOrNull(toJson: (R) -> J) = when (this) {
    is SuccessfulResult -> toJson(value)
    else -> null
}
