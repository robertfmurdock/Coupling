package com.zegreatrob.coupling.client.external.reactdnd

import kotlin.js.Json
import kotlin.js.json

fun <T> useDrag(itemType: String, itemId: Any, collect: (DragSourceMonitor) -> T): DragDropValueContent<T> {

    val results = useDragFunc(json(
            "item" to json("type" to itemType, "id" to itemId),
            "collect" to collect
    )).unsafeCast<Array<dynamic>>()

    return DragDropValueContent(
            results[0].unsafeCast<T>(),
            results[1].unsafeCast<(Any) -> Any>()
    )
}

data class DragDropValueContent<T>(val value: T, val dragFunc: (Any) -> Any)

fun <T> useDrop(acceptItemType: String, drop: (Json) -> Unit, collect: (DragSourceMonitor) -> T): DragDropValueContent<T> {

    val results = useDropFunc(json(
            "accept" to acceptItemType,
            "drop" to drop,
            "collect" to collect
    )).unsafeCast<Array<dynamic>>()

    return DragDropValueContent(
            results[0].unsafeCast<T>(),
            results[1].unsafeCast<(Any) -> Any>()
    )
}