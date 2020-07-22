package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.action.*
import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useScope
import com.zegreatrob.coupling.client.external.react.useStyles
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
    getDataAsync: suspend (ReloadFunction, CoroutineScope) -> Result<P>,
    scope: CoroutineScope,
    setData: (P?) -> Unit
) {
    val (loadingJob, setLoadingJob) = useState<Job?>(null)

    if (loadingJob == null) {
        val reloadFunction = { setData(null); setLoadingJob(null) }
        setLoadingJob(
            scope.launch {
                when (val result = getDataAsync.invoke(reloadFunction, scope)) {
                    is SuccessfulResult -> setData(result.value)
                    is NotFoundResult -> console.error("${result.entityName} Not Found")
                    is ErrorResult -> console.error(result.message)
                    is UnauthorizedResult -> console.error("Not Authorized")
                }
            }
        )
    }
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