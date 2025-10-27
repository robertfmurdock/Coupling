package com.zegreatrob.coupling.client.components.external.reactdnd

import com.zegreatrob.coupling.client.components.external.reactdnd.dsl.DragDropValueContent
import js.globals.globalThis
import kotlin.js.Json
import kotlin.js.json

fun <T> useDrag(
    itemType: String,
    itemId: String,
    endCallback: (itemId: String, dropResult: Json?) -> Unit = { _, _ -> },
): DragDropValueContent<T> {
    if (globalThis["IS_JSDOM"] == true) {
        return DragDropValueContent(null.unsafeCast<T>()) {}
    }
    val results = useDrag(
        json(
            "type" to itemType,
            "item" to json("id" to itemId),
            "end" to (
                { item: Json, monitor: DragSourceMonitor ->
                    endCallback.invoke("${item["id"]}", monitor.getDropResult())
                }
                ),
        ),
    ).unsafeCast<Array<dynamic>>()

    return DragDropValueContent(
        results[0].unsafeCast<T>(),
        results[1].unsafeCast<(Any) -> Any>(),
    )
}

fun <T> useDrop(
    acceptItemType: String,
    drop: (Json) -> Json?,
    collect: (DragSourceMonitor) -> T,
): DragDropValueContent<T> {
    if (globalThis["IS_JSDOM"] == true) {
        return DragDropValueContent(null.unsafeCast<T>()) {}
    }

    val results = useDrop(
        json(
            "accept" to acceptItemType,
            "drop" to drop,
            "collect" to collect,
        ),
    ).unsafeCast<Array<dynamic>>()

    return DragDropValueContent(
        results[0].unsafeCast<T>(),
        results[1].unsafeCast<(Any) -> Any>(),
    )
}
