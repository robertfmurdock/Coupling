package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.action.*
import com.zegreatrob.coupling.client.*
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import com.zegreatrob.testmints.action.async.execute
import react.RBuilder
import react.RClass
import react.RProps
import react.childFunction
import react.router.dom.redirect

data class CouplingLoaderProps<P : RProps>(val getDataAsync: DataLoadFunc<Result<P>>) : RProps

fun <P : RProps> dataLoadProps(getDataSync: (DataLoadComponentTools) -> P) =
    CouplingLoaderProps { tools -> getDataSync(tools).successResult() }

fun <R, P : RProps> dataLoadProps(
    query: SuspendResultAction<CommandDispatcher, R>,
    toProps: (ReloadFunction, DispatchFunc<CommandDispatcher>, R) -> P,
    commander: Commander
) = CouplingLoaderProps { tools ->
    val dispatchFunc = DecoratedDispatchFunc(commander::tracingDispatcher, tools)

    commander.tracingDispatcher().execute(query).transform { value ->
        toProps(tools.reloadData, dispatchFunc, value)
    }
}

fun <P : RProps> couplingDataLoadWrapper(component: RClass<P>) = reactFunction { props: CouplingLoaderProps<P> ->
    childFunction(
        component = dataLoadWrapper(),
        props = DataLoadWrapperProps(props.getDataAsync, ::onError)
    ) { state: DataLoadState<Result<P>> ->
        animationFrame(state, component)
    }
}

private fun <P> onError(it: Throwable) = ErrorResult<P>(it.message ?: "Data load error ${it::class}")

private fun <P : RProps> RBuilder.animationFrame(state: DataLoadState<Result<P>>, reactFunction: RClass<P>) =
    child(animationFrame, AnimationFrameProps(state)) {
        if (state is ResolvedState) {
            resolvedComponent(state, reactFunction)
        }
    }

private fun <P : RProps> RBuilder.resolvedComponent(state: ResolvedState<Result<P>>, reactFunction: RClass<P>) {
    when (val result = state.result) {
        is SuccessfulResult -> child(reactFunction, result.value)
        is NotFoundResult -> notFoundContent(result)
        is ErrorResult -> console.error("Error: ${result.message}")
        is UnauthorizedResult -> unauthorizedContent()
    }
}

private fun <P : RProps> RBuilder.notFoundContent(result: NotFoundResult<P>) = redirect(to = Paths.tribeList()).also {
    console.error("${result.entityName} was not found.")
}

private fun RBuilder.unauthorizedContent() = redirect(to = Paths.welcome()).also { console.error("Unauthorized") }
