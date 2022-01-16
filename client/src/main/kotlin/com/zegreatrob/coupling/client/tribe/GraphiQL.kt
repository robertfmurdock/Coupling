package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.minreact.child
import com.zegreatrob.react.dataloader.DataLoader
import com.zegreatrob.react.dataloader.EmptyState
import com.zegreatrob.react.dataloader.PendingState
import com.zegreatrob.react.dataloader.ResolvedState
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.w3c.dom.get
import react.ElementType
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import kotlin.js.Json
import kotlin.js.Promise

val GraphiQL: ElementType<GraphiQLProps> = FC { props ->
    val getComponent = fun() = window["GraphiQL"].unsafeCast<ElementType<GraphiQLProps>?>()

    child(DataLoader({ waitForComponent(getComponent) }, { null }) { state ->
        when (state) {
            is EmptyState -> div()
            is PendingState -> div()
            is ResolvedState -> state.result
                ?.let { component -> component { +props } }
                ?: div { +"Error finding component." }
        }
    })
}

private suspend fun waitForComponent(getComponent: () -> ElementType<GraphiQLProps>?) = withTimeout(2000) {
    while (getComponent() == null) {
        delay(1)
    }
    getComponent()
}

external interface GraphiQLProps : Props {
    var editorTheme: String
    var fetcher: (graphQlParams: Json) -> Promise<String>
}
