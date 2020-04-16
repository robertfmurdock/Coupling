package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.CommandDispatcher
import com.zegreatrob.coupling.client.CommandFunc
import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.react.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.div

interface DataLoadStyles {
    val viewFrame: String
}

inline fun <reified P : RProps> dataLoadWrapper(wrappedRComponent: RComponent<P>): RComponent<DataLoadProps<P>> =

    object : RComponent<DataLoadProps<P>>(provider()), StyledComponentRenderer<DataLoadProps<P>, DataLoadStyles>,
        ReactScopeProvider {
        private val animationContextConsumer = animationsDisabledContext.Consumer

        override val componentPath: String get() = "routing/DataLoadWrapper"

        override fun StyledRContext<DataLoadProps<P>, DataLoadStyles>.render(): ReactElement {
            val (data, setData) = useState<P?>(null)

            val (animationState, setAnimationState) = useState(AnimationState.Start)
            val shouldStartAnimation = data != null && animationState === AnimationState.Start

            val scope = useScope("Data load")

            props.getDataAsync.invokeOnScope(scope, setData)

            return reactElement {
                consumer(animationContextConsumer) { animationsDisabled: Boolean ->
                    div {
                        attrs {
                            classes += styles.viewFrame
                            if (shouldStartAnimation && !animationsDisabled) {
                                classes += "ng-enter"
                            }
                            this["onAnimationEnd"] = { setAnimationState(AnimationState.Stop) }
                        }
                        if (data != null) {
                            wrappedComponent(data)
                        }
                    }
                }
            }
        }

        private fun (suspend (ReloadFunction, CoroutineScope) -> P).invokeOnScope(
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

        private fun RBuilder.wrappedComponent(props: P) = wrappedRComponent.render(this)(props)

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