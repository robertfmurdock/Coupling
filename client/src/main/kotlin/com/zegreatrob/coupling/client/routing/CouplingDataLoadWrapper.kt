package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.action.*
import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.Paths
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import com.zegreatrob.react.dataloader.*
import com.zegreatrob.testmints.action.async.execute
import react.RBuilder
import react.RClass
import react.RProps
import react.childFunction
import react.router.dom.redirect

data class CouplingLoaderProps<P : RProps>(val getDataAsync: DataLoadFunc<Result<P>>) : RProps

fun <P : RProps> dataLoadProps(getDataSync: (DataLoaderTools) -> P) =
    CouplingLoaderProps { tools -> getDataSync(tools).successResult() }

fun <R, P : RProps> dataLoadProps(
    query: SuspendResultAction<CommandDispatcher, R>,
    toProps: (ReloadFunc, DispatchFunc<CommandDispatcher>, R) -> P,
    commander: Commander
) = CouplingLoaderProps { tools ->
    val dispatchFunc = DecoratedDispatchFunc(commander::tracingDispatcher, tools)

    commander.tracingDispatcher().execute(query).transform { value ->
        toProps(tools.reloadData, dispatchFunc, value)
    }
}

fun <P : RProps> couplingDataLoader(component: RClass<P>) = reactFunction { (getDataAsync): CouplingLoaderProps<P> ->
    childFunction(dataLoader(), DataLoadWrapperProps(getDataAsync, ::onError)) { state: DataLoadState<Result<P>> ->
        animationFrame(state, component)
    }
}

private fun <P> onError(it: Throwable) = ErrorResult<P>(it.message ?: "Data load error ${it::class}")

private fun <P : RProps> RBuilder.animationFrame(state: DataLoadState<Result<P>>, component: RClass<P>) =
    child(animationFrame, AnimationFrameProps(state)) {
        if (state is ResolvedState) {
            resolvedComponent(state, component)
        }
    }

private fun <P : RProps> RBuilder.resolvedComponent(state: ResolvedState<Result<P>>, component: RClass<P>) {
    when (val result = state.result) {
        is SuccessfulResult -> child(component, result.value)
        is NotFoundResult -> notFoundContent(result)
        is ErrorResult -> console.error("Error: ${result.message}")
        is UnauthorizedResult -> unauthorizedContent()
    }
}

private fun <P : RProps> RBuilder.notFoundContent(result: NotFoundResult<P>) = redirect(to = Paths.tribeList()).also {
    console.error("${result.entityName} was not found.")
}

private fun RBuilder.unauthorizedContent() = redirect(to = Paths.welcome()).also { console.error("Unauthorized") }
