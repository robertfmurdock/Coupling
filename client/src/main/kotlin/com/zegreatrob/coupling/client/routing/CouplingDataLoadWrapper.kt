package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.Paths
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import com.zegreatrob.react.dataloader.*
import com.zegreatrob.testmints.action.async.SuspendAction
import com.zegreatrob.testmints.action.async.execute
import react.RBuilder
import react.RClass
import react.RProps
import react.router.dom.redirect

data class CouplingLoaderProps<P : RProps>(val getDataAsync: DataLoadFunc<P?>) : RProps

fun <P : RProps> dataLoadProps(getDataSync: (DataLoaderTools) -> P) =
    CouplingLoaderProps { tools -> getDataSync(tools) }

fun <R, P : RProps> dataLoadProps(
    query: SuspendAction<CommandDispatcher, R?>,
    toProps: (ReloadFunc, DispatchFunc<CommandDispatcher>, R) -> P,
    commander: Commander
) = CouplingLoaderProps { tools ->
    val dispatchFunc = DecoratedDispatchFunc(commander::tracingDispatcher, tools)

    commander.tracingDispatcher().execute(query)?.let { value ->
        toProps(tools.reloadData, dispatchFunc, value)
    }
}

fun <P : RProps> couplingDataLoader(component: RClass<P>) = reactFunction { (getDataAsync): CouplingLoaderProps<P> ->
    dataLoader(getDataAsync, { null }) { state: DataLoadState<P?> ->
        animationFrame(state, component)
    }
}

private fun <P : RProps> RBuilder.animationFrame(state: DataLoadState<P?>, component: RClass<P>) =
    child(animationFrame, AnimationFrameProps(state)) {
        if (state is ResolvedState) {
            resolvedComponent(state, component)
        }
    }

private fun <P : RProps> RBuilder.resolvedComponent(state: ResolvedState<P?>, component: RClass<P>) {
    when (val result = state.result) {
        null -> notFoundContent()
        else -> child(component, result)
    }
}

private fun RBuilder.notFoundContent() = redirect(to = Paths.tribeList()).also {
    console.error("Data was not found.")
}
