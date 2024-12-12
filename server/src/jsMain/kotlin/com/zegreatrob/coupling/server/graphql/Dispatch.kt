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
import kotlin.js.Promise

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

typealias CommandFunc<E,I, C> = (entity: E, input: I?) -> C?

inline fun <reified E : Any, reified I : Any, reified D : TraceIdProvider, reified C, reified R, reified J> dispatch(
    crossinline dispatcherFunc: GraphQLDispatcherProvider<E, I, D>,
    crossinline commandFunc: CommandFunc<E, I, C>,
    crossinline fireFunc: suspend ActionCannon<D>.(C) -> R,
    crossinline toSerializable: (R) -> J,
) = resolver<E, I, J> { entity: E, input: I?, context: CouplingContext, _: Json ->
    val cannon = cannon<D, E, I?>(context, entity, input, dispatcherFunc)
        ?: return@resolver null
    val result = commandFunc(entity, input)?.let { cannon.fireFunc(it) }
    if (result == null) {
        null
    } else {
        toSerializable(result)
    }
}

inline fun <reified E, reified I> parseGraphJsons(entityJson: Json?, args: Json): Pair<E, I?> {
    val entity = couplingJsonFormat.decodeFromDynamic<E>(entityJson)
    val inputArg: Json? = args.at("/input")
    val input = inputArg?.let { couplingJsonFormat.decodeFromDynamic<I>(inputArg) }
    return Pair(entity, input)
}

typealias GraphQLResolver = (Json?, Json, CouplingContext, Json) -> Promise<dynamic>

inline fun <reified E, reified A, reified R> resolver(crossinline block: suspend (E, A?, CouplingContext, Json) -> R?): GraphQLResolver = { entityJson: Json?, args: Json, context: CouplingContext, queryInfo: Json ->
    val (entity, second) = parseGraphJsons<E, A>(entityJson, args)
    context.scope.promise {
        try {
            block(entity, second, context, queryInfo)
                ?.let { couplingJsonFormat.encodeToDynamic(it) }
        } catch (error: Throwable) {
            error.printStackTrace()
            throw error
        }
    }
}
