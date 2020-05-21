package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.action.SuspendResultAction
import com.zegreatrob.coupling.actionFunc.execute
import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.react.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.html.classes
import react.RProps
import react.dom.div

private val styles = useStyles("routing/DataLoadWrapper")

fun <P : RProps> dataLoadWrapper(wrappedRComponent: RComponent<P>) = reactFunction<DataLoadProps<P>> { props ->
    val (data, setData) = useState<P?>(null)

    val (animationState, setAnimationState) = useState(AnimationState.Start)
    val shouldStartAnimation = data != null && animationState === AnimationState.Start

    val scope = useScope("Data load")

    invokeOnScope(props.getDataAsync, scope, setData)

    consumer(animationsDisabledContext.Consumer) { animationsDisabled: Boolean ->
        div {
            attrs {
                classes += styles["viewFrame"]
                if (shouldStartAnimation && !animationsDisabled) {
                    classes += "ng-enter"
                }
                this["onAnimationEnd"] = { setAnimationState(AnimationState.Stop) }
            }
            if (data != null) {
                wrappedRComponent.render(this)(data)
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

fun <Q : SuspendResultAction<CommandDispatcher, R>, R, P : RProps> dataLoadProps(
    query: Q,
    toProps: (ReloadFunction, DispatchFunc<CommandDispatcher>, R) -> P,
    commander: Commander
) = DataLoadProps { reload, scope ->
    val dispatchFunc = DecoratedDispatchFunc(commander::tracingDispatcher, scope)

    val result = commander.tracingDispatcher().execute(query)

    if (result is SuccessfulResult<R>) {
        toProps(reload, dispatchFunc, result.value)
    } else throw Exception(":-(")

}