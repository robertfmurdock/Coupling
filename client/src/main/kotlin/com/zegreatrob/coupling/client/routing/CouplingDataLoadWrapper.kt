package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import com.zegreatrob.react.dataloader.DataLoadState
import com.zegreatrob.react.dataloader.DataLoader
import com.zegreatrob.react.dataloader.DataLoaderTools
import com.zegreatrob.react.dataloader.ReloadFunc
import com.zegreatrob.react.dataloader.ResolvedState
import com.zegreatrob.testmints.action.async.SuspendAction
import com.zegreatrob.testmints.action.async.execute
import react.ChildrenBuilder
import react.useCallback

data class CouplingQuery<R, P : DataProps<P>>(
    val query: SuspendAction<CommandDispatcher, R?>,
    val toDataprops: (ReloadFunc, DispatchFunc<CommandDispatcher>, R) -> P,
    val commander: Commander,
) : DataPropsBind<CouplingQuery<R, P>>(couplingQuery.unsafeCast<TMFC<CouplingQuery<R, P>>>())

interface StubDataProps : DataProps<StubDataProps>

private val couplingQuery = tmFC { props: CouplingQuery<Any, StubDataProps> ->
    val (query, toDataprops, commander) = props

    val getDataAsync: suspend (DataLoaderTools) -> StubDataProps? = useCallback { tools ->
        val dispatchFunc = DecoratedDispatchFunc(commander::tracingDispatcher, tools)
        commander.tracingDispatcher()
            .execute(query)
            ?.let { value ->
                toDataprops(tools.reloadData, dispatchFunc, value)
            }
    }
    add(
        DataLoader(getDataAsync, { null }) { state: DataLoadState<StubDataProps?> ->
            animationFrame(state)
        }
    )
}

private fun <P : DataProps<P>> ChildrenBuilder.animationFrame(state: DataLoadState<P?>) =
    animationFrame {
        this.state = state
        if (state is ResolvedState) {
            resolvedComponent(state)
        }
    }

private fun <P : DataProps<P>> ChildrenBuilder.resolvedComponent(state: ResolvedState<P?>) {
    when (val result = state.result) {
        null -> notFoundContent()
        else -> add(result)
    }
}
