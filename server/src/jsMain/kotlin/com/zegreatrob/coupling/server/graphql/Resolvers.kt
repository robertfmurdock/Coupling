package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.minjson.at
import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.testmints.action.async.SuspendAction
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax
import com.zegreatrob.testmints.action.async.execute
import kotlinx.coroutines.promise
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Json

typealias GraphQLDispatcherProvider<E, I, D> = suspend (CouplingContext, E, I) -> D?

inline fun <D : SuspendActionExecuteSyntax, Q : SuspendAction<D, R>, reified R, reified J, reified I, reified E> dispatch(
    crossinline dispatcherFunc: GraphQLDispatcherProvider<E, I, D>,
    crossinline queryFunc: (E, I) -> Q,
    crossinline toSerializable: (R) -> J,
) = { entityJson: Json, args: Json, context: CouplingContext, _: Json ->
    context.scope.promise {
        try {
            val entity = couplingJsonFormat.decodeFromDynamic<E>(entityJson)
            val input = couplingJsonFormat.decodeFromDynamic<I>(args.at("/input"))
            val command = queryFunc(entity, input)
            dispatcherFunc(context, entity, input)
                ?.execute(command)
                ?.let { encodeSuccessToJson(toSerializable, it) }
        } catch (error: Throwable) {
            error.printStackTrace()
            throw error
        }
    }
}

inline fun <reified E, reified I, reified D, reified R, reified J> dispatchAction(
    crossinline dispatcherFunc: GraphQLDispatcherProvider<E, I, D>,
    crossinline fireCommand: suspend ActionCannon<D>.(_: E, input: I) -> R,
    crossinline toSerializable: (R) -> J,
) = { entityJson: Json, args: Json, context: CouplingContext, _: Json ->
    context.scope.promise {
        try {
            val entity = couplingJsonFormat.decodeFromDynamic<E>(entityJson)
            val input = couplingJsonFormat.decodeFromDynamic<I>(args.at("/input"))
            val dispatcher = dispatcherFunc(context, entity, input) ?: return@promise null
            val cannon = ActionCannon(dispatcher)
            val result = cannon.fireCommand(entity, input)
            if (result == null) {
                result
            } else {
                encodeSuccessToJson(toSerializable, result)
            }
        } catch (error: Throwable) {
            error.printStackTrace()
            throw error
        }
    }
}

inline fun <reified J, reified R> encodeSuccessToJson(toSerializable: (R) -> J, it: R): dynamic {
    val value = toSerializable(it)
    return couplingJsonFormat.encodeToDynamic(value)
}
