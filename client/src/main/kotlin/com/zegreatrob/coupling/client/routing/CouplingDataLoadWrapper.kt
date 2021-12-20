package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.Paths
import com.zegreatrob.minreact.tmFC
import com.zegreatrob.react.dataloader.*
import com.zegreatrob.testmints.action.async.SuspendAction
import com.zegreatrob.testmints.action.async.execute
import react.ChildrenBuilder
import react.ElementType
import react.Props
import react.router.Navigate

data class CouplingLoaderProps<P : Props>(val getDataAsync: DataLoadFunc<P?>) : Props

fun <P : Props> dataLoadProps(getDataSync: (DataLoaderTools) -> P) =
    CouplingLoaderProps { tools -> getDataSync(tools) }

fun <R, P : Props> dataLoadProps(
    query: SuspendAction<CommandDispatcher, R?>,
    toProps: (ReloadFunc, DispatchFunc<CommandDispatcher>, R) -> P,
    commander: Commander
) = CouplingLoaderProps { tools ->
    val dispatchFunc = DecoratedDispatchFunc(commander::tracingDispatcher, tools)

    commander.tracingDispatcher().execute(query)?.let { value ->
        toProps(tools.reloadData, dispatchFunc, value)
    }
}

fun <P : Props> couplingDataLoader(component: ElementType<P>) = tmFC { (getDataAsync): CouplingLoaderProps<P> ->
    dataLoader<P>()() {
        +DataLoaderProps(getDataAsync, { null }) { state: DataLoadState<P?> ->
            animationFrame(state, component)
        }
    }
}

private fun <P : Props> ChildrenBuilder.animationFrame(state: DataLoadState<P?>, component: ElementType<P>) =
    animationFrame {
        this.state = state
        if (state is ResolvedState) {
            resolvedComponent(state, component)
        }
    }

private fun <P : Props> ChildrenBuilder.resolvedComponent(state: ResolvedState<P?>, component: ElementType<P>) {
    when (val result = state.result) {
        null -> notFoundContent()
        else -> child(component, result)
    }
}

private fun ChildrenBuilder.notFoundContent() = Navigate { this.to = Paths.tribeList() }.also {
    console.error("Data was not found.")
}
