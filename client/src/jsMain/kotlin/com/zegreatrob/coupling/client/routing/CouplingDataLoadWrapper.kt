package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.ClientDispatcher
import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.demo.LoadingPage
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import com.zegreatrob.react.dataloader.DataLoadState
import com.zegreatrob.react.dataloader.DataLoader
import com.zegreatrob.react.dataloader.DataLoaderTools
import com.zegreatrob.react.dataloader.EmptyState
import com.zegreatrob.react.dataloader.PendingState
import com.zegreatrob.react.dataloader.ReloadFunc
import com.zegreatrob.react.dataloader.ResolvedState
import com.zegreatrob.testmints.action.async.SuspendAction
import react.Props
import react.PropsWithValue
import react.ReactNode
import react.create
import react.useCallback

external interface CouplingQueryProps<R> : Props {
    var query: SuspendAction<ClientDispatcher, R?>
    var toNode: (ReloadFunc, DispatchFunc<ClientDispatcher>, R) -> ReactNode?
    var commander: Commander
}

@ReactFunc
val CouplingQuery by nfc<CouplingQueryProps<Any>> { props ->
    val (query, toNode, commander) = props

    val getDataAsync: suspend (DataLoaderTools) -> ReactNode? = useCallback(query, toNode, commander) { tools ->
        commander.tracingCannon()
            .fire(query)
            ?.let { value ->
                toNode(
                    tools.reloadData,
                    DecoratedDispatchFunc({ commander.tracingCannon() }, tools),
                    value,
                )
            }
    }
    DataLoader(getDataAsync, { null }, child = { CouplingQueryLoadState.create { value = it } })
}

val CouplingQueryLoadState by nfc<PropsWithValue<DataLoadState<ReactNode?>>> { props ->
    when (val state = props.value) {
        is EmptyState -> LoadingPage()
        is PendingState -> LoadingPage()
        is ResolvedState -> when (val result = state.result) {
            null -> notFoundContent()
            else -> animationFrame(state) { +result }
        }
    }
}
