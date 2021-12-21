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

data class CouplingLoaderProps<P : DataProps>(val getDataAsync: DataLoadFunc<P?>) : DataProps

fun <P : DataProps> dataLoadProps(getDataSync: (DataLoaderTools) -> P) =
    CouplingLoaderProps { tools -> getDataSync(tools) }

fun <R, P : DataProps> dataLoadProps(
    query: SuspendAction<CommandDispatcher, R?>,
    toProps: (ReloadFunc, DispatchFunc<CommandDispatcher>, R) -> P,
    commander: Commander
) = CouplingLoaderProps { tools ->
    val dispatchFunc = DecoratedDispatchFunc(commander::tracingDispatcher, tools)

    commander.tracingDispatcher().execute(query)?.let { value ->
        toProps(tools.reloadData, dispatchFunc, value)
    }
}

fun <P : DataProps> couplingDataLoader(component: TMFC<P>) = tmFC { (getDataAsync): CouplingLoaderProps<P> ->
    child(dataLoader(), DataLoaderProps(getDataAsync, { null }) { state: DataLoadState<P?> ->
        animationFrame(state, component)
    })
}

private fun <P : DataProps> ChildrenBuilder.animationFrame(state: DataLoadState<P?>, component: TMFC<P>) =
    animationFrame {
        this.state = state
        if (state is ResolvedState) {
            resolvedComponent(state, component)
        }
    }

private fun <P : DataProps> ChildrenBuilder.resolvedComponent(state: ResolvedState<P?>, component: TMFC<P>) {
    when (val result = state.result) {
        null -> notFoundContent()
        else -> child(component, result)
    }
}

private fun ChildrenBuilder.notFoundContent() = Navigate { this.to = Paths.tribeList() }.also {
    console.error("Data was not found.")
}
