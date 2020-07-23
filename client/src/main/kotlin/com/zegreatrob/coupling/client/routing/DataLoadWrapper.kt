package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.action.*
import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.Paths
import com.zegreatrob.coupling.client.external.react.useScope
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import com.zegreatrob.testmints.action.async.execute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import react.*
import react.router.dom.redirect

sealed class DataLoadState<D>

class EmptyState<D> : DataLoadState<D>()

data class PendingState<D>(val job: Job) : DataLoadState<D>()

data class ResolvedState<D>(val result: D) : DataLoadState<D>()

fun <P : RProps> dataLoadWrapper(reactFunction: RClass<P>) = reactFunction { props: DataLoadProps<P> ->
    dLW(
        getDataAsync = props.getDataAsync,
        jobErrorState = { ErrorResult(it.message ?: "Data load error ${it::class}") },
        resolvedHandler = { state -> animationFrame(state, reactFunction) }
    )
}

private fun <P : RProps> RBuilder.animationFrame(state: DataLoadState<Result<P>>, reactFunction: RClass<P>) {
    child(animationFrame, AnimationFrameProps(state)) {
        if (state is ResolvedState) {
            resolvedComponent(state, reactFunction)
        }
    }
}

private fun <D> RBuilder.dLW(
    getDataAsync: DataloadPropsFunc<D>,
    jobErrorState: (Throwable) -> D,
    resolvedHandler: RBuilder.(DataLoadState<D>) -> Unit
) {
    val (state, setState) = useState<DataLoadState<D>> { EmptyState() }

    val scope = useScope("Data load")

    if (state is EmptyState) {
        startPendingJob(setState, scope, getDataAsync, jobErrorState)
    }

    resolvedHandler(state)
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

private fun <D> startPendingJob(
    setState: RSetState<DataLoadState<D>>,
    scope: CoroutineScope,
    getDataAsync: DataloadPropsFunc<D>,
    jobErrorState: (Throwable) -> D
) {
    val setEmpty = setState.empty()
    val setPending = setState.pending()
    val setResolved = setState.resolved()
    setPending(
        scope.launch { getDataAsync(setEmpty, scope).let(setResolved) }
            .also { job -> job.errorOnJobFailure(setResolved, jobErrorState) }
    )
}

private fun <D> Job.errorOnJobFailure(setResolved: (D) -> Unit, errorResult: (Throwable) -> D) =
    invokeOnCompletion { cause ->
        if (cause != null) {
            setResolved(errorResult(cause))
        }
    }

private fun <D> RSetState<DataLoadState<D>>.empty(): () -> Unit = { this(EmptyState()) }
private fun <D> RSetState<DataLoadState<D>>.pending(): (Job) -> Unit = { this(PendingState(it)) }
private fun <D> RSetState<DataLoadState<D>>.resolved(): (D) -> Unit = { this(ResolvedState(it)) }

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