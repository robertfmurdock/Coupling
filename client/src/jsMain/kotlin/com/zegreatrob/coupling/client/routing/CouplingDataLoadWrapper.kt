package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.coupling.client.create
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import com.zegreatrob.react.dataloader.DataLoadState
import com.zegreatrob.react.dataloader.DataLoader
import com.zegreatrob.react.dataloader.DataLoaderTools
import com.zegreatrob.react.dataloader.ReloadFunc
import com.zegreatrob.react.dataloader.ResolvedState
import com.zegreatrob.testmints.action.async.SuspendAction
import com.zegreatrob.testmints.action.async.execute
import react.ChildrenBuilder
import react.Props
import react.ReactNode
import react.create
import react.useCallback

fun <P : DataProps<P>, R> ChildrenBuilder.CouplingQuery(
    query: SuspendAction<CommandDispatcher, R?>,
    toDataprops: (ReloadFunc, DispatchFunc<CommandDispatcher>, R) -> P?,
    commander: Commander,
) = CouplingQuery(
    query = query,
    toNode = { r: ReloadFunc, d: DispatchFunc<CommandDispatcher>, result: R ->
        toDataprops(r, d, result)?.create()
    },
    commander = commander,
)

fun <R> ChildrenBuilder.CouplingQuery(
    query: SuspendAction<CommandDispatcher, R?>,
    build: ChildrenBuilder.(ReloadFunc, DispatchFunc<CommandDispatcher>, R) -> Unit,
    commander: Commander,
) = CouplingQuery(
    query = query,
    toNode = { r: ReloadFunc, d: DispatchFunc<CommandDispatcher>, result: R ->
        react.Fragment.create { build(r, d, result) }
    },
    commander = commander,
)

external interface CouplingQueryProps<R> : Props {
    var query: SuspendAction<CommandDispatcher, R?>
    var toNode: (ReloadFunc, DispatchFunc<CommandDispatcher>, R) -> ReactNode?
    var commander: Commander
}

@ReactFunc
val CouplingQuery by nfc<CouplingQueryProps<Any>> { props ->
    val (query, toDataprops, commander) = props

    val getDataAsync: suspend (DataLoaderTools) -> ReactNode? = useCallback { tools ->
        val dispatchFunc = DecoratedDispatchFunc(commander::tracingDispatcher, tools)
        commander.tracingDispatcher()
            .execute(query)
            ?.let { value ->
                toDataprops(tools.reloadData, dispatchFunc, value)
            }
    }
    add(
        DataLoader(getDataAsync, { null }) { state: DataLoadState<ReactNode?> ->
            animationFrame(state)
        },
    )
}

private fun <P : DataProps<P>> ChildrenBuilder.animationFrame(state: DataLoadState<ReactNode?>) =
    animationFrame {
        this.state = state
        if (state is ResolvedState) {
            resolvedComponent(state)
        }
    }

private fun ChildrenBuilder.resolvedComponent(state: ResolvedState<ReactNode?>) {
    when (val result = state.result) {
        null -> notFoundContent()
        else -> +result
    }
}
