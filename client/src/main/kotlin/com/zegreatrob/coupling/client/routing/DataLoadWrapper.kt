package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.action.*
import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useScope
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import com.zegreatrob.testmints.action.async.execute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.html.classes
import react.*
import react.dom.div

private val styles = useStyles("routing/DataLoadWrapper")

sealed class DataLoadState<P : RProps>

class EmptyState<P : RProps> : DataLoadState<P>()

data class PendingState<P : RProps>(val job: Job) : DataLoadState<P>()

data class ResolvedState<P : RProps>(val result: Result<P>) : DataLoadState<P>()

fun <P : RProps> dataLoadWrapper(reactFunction: RClass<P>) = reactFunction { props: DataLoadProps<P> ->
    val (state, setState) = useState<DataLoadState<P>> { EmptyState() }
    val (animationState, setAnimationState) = useState(AnimationState.Start)
    val scope = useScope("Data load")

    if (state is EmptyState) {
        startPendingJob(setState, scope, props.getDataAsync)
    }

    val shouldStartAnimation = state !is EmptyState && animationState === AnimationState.Start

    animationsDisabledContext.Consumer { animationsDisabled: Boolean ->
        div {
            attrs {
                classes += styles["viewFrame"]
                if (shouldStartAnimation && !animationsDisabled) {
                    classes += "ng-enter"
                }
                this["onAnimationEnd"] = { setAnimationState(AnimationState.Stop) }
            }
            if (state is ResolvedState) {
                resolvedComponent(state, reactFunction)
            }
        }
    }
}

private fun <P : RProps> RBuilder.resolvedComponent(
    state: ResolvedState<P>,
    reactFunction: RClass<P>
) {
    when (val result = state.result) {
        is SuccessfulResult -> child(reactFunction, result.value)
        is NotFoundResult -> console.error("${result.entityName} was not found.")
        is ErrorResult -> console.error("Error: ${result.message}")
        is UnauthorizedResult -> console.error("Unauthorized")
    }
}

private fun <P : RProps> startPendingJob(
    setState: RSetState<DataLoadState<P>>,
    scope: CoroutineScope,
    getDataAsync: DataloadPropsFunc<Result<P>>
) {
    val setEmpty = setState.empty()
    val setPending = setState.pending()
    val setResolved = setState.resolved()
    setPending(
        scope.launch { getDataAsync(setEmpty, scope).let(setResolved) }
            .also { job -> job.errorOnJobFailure(setResolved) }
    )
}

private fun <P : RProps> Job.errorOnJobFailure(setResolved: (Result<P>) -> Unit) = invokeOnCompletion { cause ->
    if (cause != null) {
        setResolved(ErrorResult(cause.message ?: "Data load error ${cause::class}"))
    }
}

private fun <P : RProps> RSetState<DataLoadState<P>>.empty(): () -> Unit = { this(EmptyState()) }
private fun <P : RProps> RSetState<DataLoadState<P>>.pending(): (Job) -> Unit = { this(PendingState(it)) }
private fun <P : RProps> RSetState<DataLoadState<P>>.resolved(): (Result<P>) -> Unit = { this(ResolvedState(it)) }

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