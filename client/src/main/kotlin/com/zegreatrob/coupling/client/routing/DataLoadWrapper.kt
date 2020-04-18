package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.CommandFunc
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

    props.getDataAsync.invokeOnScope(scope, setData)

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

private fun <P> (suspend (ReloadFunction, CoroutineScope) -> P).invokeOnScope(
    scope: CoroutineScope,
    setData: (P?) -> Unit
) {
    val (loadingJob, setLoadingJob) = useState<Job?>(null)

    if (loadingJob == null) {
        val reloadFunction = { setData(null); setLoadingJob(null) }
        setLoadingJob(
            scope.launch {
                setData(this@invokeOnScope.invoke(reloadFunction, scope))
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

fun <D, P : RProps> dataLoadProps(
    query: suspend () -> D,
    toProps: (ReloadFunction, CoroutineScope, D) -> P
): DataLoadProps<P> = DataLoadProps { reload, scope -> query().let { result -> toProps(reload, scope, result) } }


fun <D, P : RProps> dataLoadProps(
    query: suspend CommandDispatcher.() -> D,
    toProps: (ReloadFunction, CommandFunc<CommandDispatcher>, D) -> P,
    commander: Commander
) = DataLoadProps { reload, scope ->
    val result = commander.runQuery(query)
    toProps(reload, commander.buildCommandFunc(scope), result)
}