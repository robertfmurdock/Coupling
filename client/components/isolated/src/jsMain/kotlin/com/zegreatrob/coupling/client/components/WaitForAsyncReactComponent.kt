package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import com.zegreatrob.react.dataloader.DataLoadState
import com.zegreatrob.react.dataloader.DataLoader
import com.zegreatrob.react.dataloader.EmptyState
import com.zegreatrob.react.dataloader.PendingState
import com.zegreatrob.react.dataloader.ResolvedState
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import react.ChildrenBuilder
import react.ElementType
import react.Fragment
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML
import kotlin.let

fun <T : Props> ChildrenBuilder.waitForAsyncReactComponent(
    getComponent: () -> ElementType<T>?,
    useComponent: (ElementType<T>) -> ReactNode,
) {
    DataLoader({ waitForComponent(getComponent) }, { null }, child = { state ->
        Fragment.create {
            AsyncReactComponent(state, useComponent)
        }
    })
}

external interface AsyncReactComponentProps<T : Props> : Props {
    var state: DataLoadState<ElementType<T>?>
    var useComponent: (ElementType<T>) -> ReactNode
}

@ReactFunc
val AsyncReactComponent by nfc<AsyncReactComponentProps<Props>> { props ->
    val state = props.state
    val useComponent = props.useComponent
    when (state) {
        is EmptyState -> ReactHTML.div { +"Preparing component" }
        is PendingState -> ReactHTML.div { +"Pending component" }
        is ResolvedState ->
            state.result
                ?.let { +useComponent(it) }
                ?: ReactHTML.div { +"Error finding component." }
    }
}

private suspend fun <T : Props> waitForComponent(getComponent: () -> ElementType<T>?): ElementType<T>? = withTimeout(2000) {
    while (getComponent() == null) {
        delay(5)
    }
    getComponent()
}
