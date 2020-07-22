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
import react.RClass
import react.RProps
import react.dom.div
import react.useState

private val styles = useStyles("routing/DataLoadWrapper")

fun <P : RProps> dataLoadWrapper(reactFunction: RClass<P>) = reactFunction { props: DataLoadProps<P> ->
    val (result, setResult) = useState<Result<P>?>(null)
    val (animationState, setAnimationState) = useState(AnimationState.Start)
    val shouldStartAnimation = result != null && animationState === AnimationState.Start

    val scope = useScope("Data load")

    invokeOnScope(scope, setResult) { reloadFunc -> props.getDataAsync.invoke(reloadFunc, scope) }

    animationsDisabledContext.Consumer { animationsDisabled: Boolean ->
        div {
            attrs {
                classes += styles["viewFrame"]
                if (shouldStartAnimation && !animationsDisabled) {
                    classes += "ng-enter"
                }
                this["onAnimationEnd"] = { setAnimationState(AnimationState.Stop) }
            }
            when (result) {
                is SuccessfulResult -> child(reactFunction, result.value)
                is NotFoundResult -> console.error("${result.entityName} was not found.")
                is ErrorResult -> console.error("Error: ${result.message}")
                is UnauthorizedResult -> console.error("Unauthorized")
            }
        }
    }
}

private fun <P> invokeOnScope(
    scope: CoroutineScope,
    setResult: (Result<P>?) -> Unit,
    performQuery: suspend (ReloadFunction) -> Result<P>
) {
    val (loadingJob, setLoadingJob) = useState<Job?>(null)
    if (loadingJob == null) {
        val reloadFunction = { setResult(null); setLoadingJob(null) }
        setLoadingJob(
            loadingJob(scope, setResult) { performQuery(reloadFunction) }
        )
    }
}

private fun <P> loadingJob(
    scope: CoroutineScope,
    setResult: (Result<P>?) -> Unit,
    queryFunc: suspend () -> Result<P>
) = scope.launch {
    queryFunc().let(setResult)
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