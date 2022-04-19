package com.zegreatrob.coupling.client.external.reactdnd

import kotlin.js.Json
import kotlin.js.json

fun <T> useDrag(itemType: String, itemId: Any): DragDropValueContent<T> {
    val results = useDragFunc(
        json(
            "type" to itemType,
            "item" to json("id" to itemId),
        )
    ).unsafeCast<Array<dynamic>>()

    return DragDropValueContent(
        results[0].unsafeCast<T>(),
        results[1].unsafeCast<(Any) -> Any>()
    )
}

data class DragDropValueContent<T>(val value: T, val dragFunc: (Any) -> Any)

fun <T> useDrop(
    acceptItemType: String,
    drop: (Json) -> Unit,
    collect: (DragSourceMonitor) -> T
): DragDropValueContent<T> {

    val results = useDropFunc(
        json(
            "accept" to acceptItemType,
            "drop" to drop,
            "collect" to collect
        )
    ).unsafeCast<Array<dynamic>>()

    return DragDropValueContent(
        results[0].unsafeCast<T>(),
        results[1].unsafeCast<(Any) -> Any>()
    )
}
