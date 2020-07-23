package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.action.*
import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.Paths
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import com.zegreatrob.testmints.action.async.execute
import kotlinx.coroutines.CoroutineScope
import react.RBuilder
import react.RClass
import react.RProps
import react.childFunction
import react.router.dom.redirect

fun <P : RProps> dataLoadWrapper(reactFunction: RClass<P>) = reactFunction { props: DataLoadProps<P> ->
    childFunction(
        component = dLW(),
        props = DLWProps(props.getDataAsync, { ErrorResult(it.message ?: "Data load error ${it::class}") })
    ) { state: DataLoadState<Result<P>> ->
        animationFrame(state, reactFunction)
    }
}

private fun <P : RProps> RBuilder.animationFrame(state: DataLoadState<Result<P>>, reactFunction: RClass<P>) {
    child(animationFrame, AnimationFrameProps(state)) {
        if (state is ResolvedState) {
            resolvedComponent(state, reactFunction)
        }
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

private fun <P : RProps> RBuilder.notFoundContent(result: NotFoundResult<P>) {
    console.error("${result.entityName} was not found.")
    redirect(to = Paths.tribeList())
}

private fun RBuilder.unauthorizedContent() {
    console.error("Unauthorized")
    redirect(to = Paths.welcome())
}

enum class AnimationState {
    Start, Stop
}

typealias ReloadFunction = () -> Unit

typealias DataloadPropsFunc<P> = suspend (ReloadFunction, CoroutineScope) -> P

data class DataLoadProps<P : RProps>(val getDataAsync: DataloadPropsFunc<Result<P>>) : RProps

fun <P : RProps> dataLoadProps(getDataSync: (ReloadFunction, CoroutineScope) -> P) = DataLoadProps { reload, scope ->
    getDataSync(reload, scope)
        .successResult()
}

fun <R, P : RProps> dataLoadProps(
    query: SuspendResultAction<CommandDispatcher, R>,
    toProps: (ReloadFunction, DispatchFunc<CommandDispatcher>, R) -> P,
    commander: Commander
) = DataLoadProps { reload, scope ->
    val dispatchFunc = dispatchFunc(commander, scope)

    commander.tracingDispatcher().execute(query).transform { value ->
        toProps(reload, dispatchFunc, value)
    }
}

private fun dispatchFunc(commander: Commander, scope: CoroutineScope) =
    DecoratedDispatchFunc(commander::tracingDispatcher, scope)