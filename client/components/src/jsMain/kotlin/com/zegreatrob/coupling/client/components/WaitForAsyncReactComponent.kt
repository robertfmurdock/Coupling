package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.create
import com.zegreatrob.react.dataloader.DataLoader
import com.zegreatrob.react.dataloader.EmptyState
import com.zegreatrob.react.dataloader.PendingState
import com.zegreatrob.react.dataloader.ResolvedState
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import react.ChildrenBuilder
import react.ElementType
import react.Props
import react.dom.html.ReactHTML.div

fun <T : Props> ChildrenBuilder.waitForAsyncReactComponent(
    getComponent: () -> ElementType<T>?,
    useComponent: ChildrenBuilder.(ElementType<T>) -> Unit,
) {
    +DataLoader({ waitForComponent(getComponent) }, { null }) { state ->
        when (state) {
            is EmptyState -> div { +"Preparing component" }
            is PendingState -> div { +"Pending component" }
            is ResolvedState ->
                state.result
                    ?.let { useComponent(it) }
                    ?: div { +"Error finding component." }
        }
    }.create()
}

private suspend fun <T : Props> waitForComponent(getComponent: () -> ElementType<T>?): ElementType<T>? =
    withTimeout(2000) {
        while (getComponent() == null) {
            delay(5)
        }
        getComponent()
    }
