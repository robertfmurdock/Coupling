package com.zegreatrob.coupling.client.components.external.reactdnd

import com.zegreatrob.coupling.client.components.external.reactdnd.dsl.DragDropValueContent
import com.zegreatrob.coupling.client.components.waitForAsyncReactComponent
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import react.ElementType
import react.FC
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

val reactDndPromise = js("import(\"react-dnd\")")

val reactDnd = MainScope().async {
    reactDndPromise.unsafeCast<Promise<ReactDnd>>().await()
}

val DndProvider: ElementType<DnDProvideProps> = FC { props ->
    if (js("global.IS_JSDOM") == true) {
        +props.children
    } else {
        waitForAsyncReactComponent({ runCatching { reactDnd.getCompleted().dndProvider }.getOrNull() }) { component ->
            component { +props }
        }
    }
}

fun <T> useDrag(itemType: String, itemId: Any): DragDropValueContent<T> {
    if (js("global.IS_JSDOM") == true) {
        return DragDropValueContent(null.unsafeCast<T>()) {}
    }
    val results = reactDnd.getCompleted().useDrag(
        json(
            "type" to itemType,
            "item" to json("id" to itemId),
        ),
    ).unsafeCast<Array<dynamic>>()

    return DragDropValueContent(
        results[0].unsafeCast<T>(),
        results[1].unsafeCast<(Any) -> Any>(),
    )
}

fun <T> useDrop(
    acceptItemType: String,
    drop: (Json) -> Unit,
    collect: (DragSourceMonitor) -> T,
): DragDropValueContent<T> {
    if (js("global.IS_JSDOM") == true) {
        return DragDropValueContent(null.unsafeCast<T>()) {}
    }

    val results = reactDnd.getCompleted().useDrop(
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

external interface ReactDnd {
    fun useDrag(options: Json): dynamic
    fun useDrop(options: Json): dynamic

    @JsName("DndProvider")
    val dndProvider: ElementType<DnDProvideProps>
}
