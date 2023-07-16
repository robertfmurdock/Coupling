package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import com.zegreatrob.react.dataloader.DataLoadState
import com.zegreatrob.react.dataloader.DataLoader
import com.zegreatrob.react.dataloader.DataLoaderTools
import com.zegreatrob.react.dataloader.ReloadFunc
import com.zegreatrob.react.dataloader.ResolvedState
import com.zegreatrob.testmints.action.async.SuspendAction
import react.Props
import react.PropsWithValue
import react.ReactNode
import react.create
import react.useCallback

external interface CouplingQueryProps<R> : Props {
    var query: SuspendAction<CouplingSdkDispatcher, R?>
    var toNode: (ReloadFunc, DispatchFunc<CouplingSdkDispatcher>, R) -> ReactNode?
    var commander: Commander
}

@ReactFunc
val CouplingQuery by nfc<CouplingQueryProps<Any>> { props ->
    val (query, toNode, commander) = props

    val getDataAsync: suspend (DataLoaderTools) -> ReactNode? = useCallback { tools ->
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
    DataLoader(getDataAsync, { null }, child = CouplingQueryLoadState::create)
}

val CouplingQueryLoadState by nfc<PropsWithValue<DataLoadState<ReactNode?>>> { props ->
    val state = props.value
    animationFrame {
        this.state = state
        if (state is ResolvedState) {
            when (val result = state.result) {
                null -> notFoundContent()
                else -> +result
            }
        }
    }
}
