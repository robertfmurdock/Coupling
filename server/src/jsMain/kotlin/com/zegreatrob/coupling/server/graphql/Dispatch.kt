package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.action.LoggingActionPipe
import com.zegreatrob.coupling.action.TraceIdProvider
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.minjson.at
import com.zegreatrob.testmints.action.ActionCannon
import kotlinx.coroutines.promise
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Json

typealias GraphQLDispatcherProvider<E, I, D> = suspend (CouplingContext, E, I) -> D?

suspend inline fun <D : TraceIdProvider, reified E, reified I> cannon(
    context: CouplingContext,
    entity: E,
    input: I,
    dispatcherFunc: GraphQLDispatcherProvider<E, I, D>,
): ActionCannon<D>? {
    val dispatcher = dispatcherFunc(context, entity, input)
        ?: return null
    return ActionCannon(dispatcher, LoggingActionPipe(dispatcher.traceId))
}

inline fun <reified E, reified I, reified D : TraceIdProvider, reified C, reified R, reified J> dispatch(
    crossinline dispatcherFunc: GraphQLDispatcherProvider<E, I, D>,
    crossinline commandFunc: (_: E, input: I) -> C,
    crossinline fireFunc: suspend ActionCannon<D>.(C) -> R,
    crossinline toSerializable: (R) -> J,
) = { entityJson: Json?, args: Json, context: CouplingContext, queryInfo: Json ->
    context.scope.promise {
        try {
            val targetField = queryInfo["fieldName"].toString()
            val alreadyLoadedField = entityJson?.get(targetField)
            if (alreadyLoadedField != null) {
                return@promise alreadyLoadedField
            }
            val (entity, input) = parseGraphJsons<E, I>(entityJson, args)
            val cannon = cannon(context, entity, input, dispatcherFunc)
                ?: return@promise null
            val result = cannon.fireFunc(commandFunc(entity, input))
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

inline fun <reified E, reified I> parseGraphJsons(entityJson: Json?, args: Json): Pair<E, I> {
    val entity = couplingJsonFormat.decodeFromDynamic<E>(entityJson)
    val input = couplingJsonFormat.decodeFromDynamic<I>(args.at("/input"))
    return Pair(entity, input)
}

inline fun <reified J, reified R> encodeSuccessToJson(toSerializable: (R) -> J, it: R): dynamic {
    val value = toSerializable(it)
    return couplingJsonFormat.encodeToDynamic(value)
}
