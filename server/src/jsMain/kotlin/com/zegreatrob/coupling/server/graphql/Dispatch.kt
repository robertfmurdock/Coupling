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

typealias GraphQLDispatcherProvider<E, I, D> = suspend (CouplingContext, E, I?) -> D?

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

typealias CommandFunc<reified E, reified I, reified C> = (entity: E, input: I?) -> C?

inline fun <reified E : Any, reified I : Any, reified D : TraceIdProvider, reified C, reified R, reified J> dispatch(
    crossinline dispatcherFunc: GraphQLDispatcherProvider<E, I, D>,
    crossinline commandFunc: CommandFunc<E, I, C>,
    crossinline fireFunc: suspend ActionCannon<D>.(C) -> R,
    crossinline toSerializable: (R) -> J,
) = { entityJson: Json?, args: Json, context: CouplingContext, queryInfo: Json ->
    context.scope.promise {
        try {
            val targetField = queryInfo["fieldName"].toString()
            println("targetField $targetField")
            val alreadyLoadedField = entityJson?.get(targetField)
            if (alreadyLoadedField != null) {
                return@promise alreadyLoadedField
            }
            println("entityJson ${JSON.stringify(entityJson)} args ${JSON.stringify(args)}")
            val (entity, input) = parseGraphJsons<E, I>(entityJson, args)
            println("entity $entity input $input")
            val cannon = cannon<D, E, I?>(context, entity, input, dispatcherFunc)
                ?: return@promise null
            val result = commandFunc(entity, input)?.let { cannon.fireFunc(it) }
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

inline fun <reified E, reified I> parseGraphJsons(entityJson: Json?, args: Json): Pair<E, I?> {
    val entity = couplingJsonFormat.decodeFromDynamic<E>(entityJson)
    val inputArg: Json? = args.at("/input")
    val input = inputArg?.let { couplingJsonFormat.decodeFromDynamic<I>(inputArg) }
    return Pair(entity, input)
}

inline fun <reified J, reified R> encodeSuccessToJson(toSerializable: (R) -> J, it: R): dynamic {
    val value = toSerializable(it)
    return couplingJsonFormat.encodeToDynamic(value)
}
