package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.Paths
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import com.zegreatrob.react.dataloader.*
import com.zegreatrob.testmints.action.async.SuspendAction
import com.zegreatrob.testmints.action.async.execute
import react.ChildrenBuilder
import react.router.Navigate

data class CouplingLoaderProps<P : DataProps<P>>(override val component: TMFC<CouplingLoaderProps<P>>, val getDataAsync: DataLoadFunc<P?>) :
    DataProps<CouplingLoaderProps<P>>

fun <P : DataProps<P>> dataLoadProps(component: TMFC<CouplingLoaderProps<P>>, getDataSync: (DataLoaderTools) -> P) =
    CouplingLoaderProps(component) { tools -> getDataSync(tools) }

fun <R, P : DataProps<P>> dataLoadProps(
    component: TMFC<CouplingLoaderProps<P>>,
    query: SuspendAction<CommandDispatcher, R?>,
    toProps: (ReloadFunc, DispatchFunc<CommandDispatcher>, R) -> P,
    commander: Commander
) = CouplingLoaderProps(component) { tools ->
    val dispatchFunc = DecoratedDispatchFunc(commander::tracingDispatcher, tools)

    commander.tracingDispatcher().execute(query)?.let { value ->
        toProps(tools.reloadData, dispatchFunc, value)
    }
}

fun <P : DataProps<P>> couplingDataLoader() = tmFC { (_, getDataAsync): CouplingLoaderProps<P> ->
    child(DataLoader(getDataAsync, { null }) { state: DataLoadState<P?> ->
        animationFrame(state)
    })
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
        else -> child(result)
    }
}

private fun ChildrenBuilder.notFoundContent() = Navigate { this.to = Paths.welcome() }.also {
    console.error("Data was not found.")
}
