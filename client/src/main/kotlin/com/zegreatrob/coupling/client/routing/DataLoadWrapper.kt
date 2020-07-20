package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.action.SuspendResultAction
import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.minreact.reactFunction
import com.zegreatrob.coupling.client.external.react.useScope
import com.zegreatrob.coupling.client.external.react.useStyles
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

fun <P : RProps> dataLoadWrapper(reactFunction: RClass<P>) =
    reactFunction<DataLoadProps<P>> { props ->
        val (data, setData) = useState<P?>(null)

        val (animationState, setAnimationState) = useState(AnimationState.Start)
        val shouldStartAnimation = data != null && animationState === AnimationState.Start

        val scope = useScope("Data load")

        invokeOnScope(props.getDataAsync, scope, setData)

        animationsDisabledContext.Consumer { animationsDisabled: Boolean ->
            div {
                attrs {
                    classes += styles["viewFrame"]
                    if (shouldStartAnimation && !animationsDisabled) {
                        classes += "ng-enter"
                    }
                    this["onAnimationEnd"] = { setAnimationState(AnimationState.Stop) }
                }
                if (data != null) {
                    child(
                        type = reactFunction,
                        props = data,
                        handler = { }
                    )
                }
            }
        }
    }

private fun <P> invokeOnScope(
    getDataAsync: suspend (ReloadFunction, CoroutineScope) -> P,
    scope: CoroutineScope,
    setData: (P?) -> Unit
) {
    val (loadingJob, setLoadingJob) = useState<Job?>(null)

    if (loadingJob == null) {
        val reloadFunction = { setData(null); setLoadingJob(null) }
        setLoadingJob(
            scope.launch {
                setData(getDataAsync.invoke(reloadFunction, scope))
            }
        )
    }
}

enum class AnimationState {
    Start, Stop
}

typealias ReloadFunction = () -> Unit

typealias DataloadPropsFunc<P> = suspend (ReloadFunction, CoroutineScope) -> P

data class DataLoadProps<P : RProps>(val getDataAsync: DataloadPropsFunc<P>) : RProps

fun <R, P : RProps> dataLoadProps(
    query: SuspendResultAction<CommandDispatcher, R>,
    toProps: (ReloadFunction, DispatchFunc<CommandDispatcher>, R) -> P,
    commander: Commander
) = DataLoadProps { reload, scope ->
    val dispatchFunc = dispatchFunc(commander, scope)

    when (val result: Result<R> = commander.tracingDispatcher().execute(query)) {
        is SuccessfulResult<R> -> toProps(reload, dispatchFunc, result.value)
        else -> throw Exception(":-(")
    }
}

private fun dispatchFunc(commander: Commander, scope: CoroutineScope) =
    DecoratedDispatchFunc(commander::tracingDispatcher, scope)