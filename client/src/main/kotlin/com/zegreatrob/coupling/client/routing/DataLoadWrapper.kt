package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.common.ScopeProvider
import kotlinx.coroutines.*
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
        ScopeProvider {
        private val animationContextConsumer = animationsDisabledContext.Consumer

        override val componentPath: String get() = "routing/DataLoadWrapper"

        override fun StyledRContext<DataLoadProps<P>, DataLoadStyles>.render(): ReactElement {
            val (data, setData) = useState<P?>(null)

            val (animationState, setAnimationState) = useState(AnimationState.Start)
            val shouldStartAnimation = data != null && animationState === AnimationState.Start

            props.getDataAsync.invokeOnScope(setData)

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

        private fun (suspend (ReloadFunction) -> P).invokeOnScope(setData: (P?) -> Unit) {
            val (scope) = useState { buildScope() + CoroutineName("Data Load") }
            useEffectWithCleanup(arrayOf()) {
                { scope.cancel() }
            }

            val (loadingJob, setLoadingJob) = useState<Job?>(null)

            if (loadingJob == null) {
                val reloadFunction = { setData(null); setLoadingJob(null) }
                setLoadingJob(
                    scope.launch {
                        setData(this@invokeOnScope.invoke(reloadFunction))
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

data class DataLoadProps<P : RProps>(val getDataAsync: suspend (ReloadFunction) -> P) : RProps

fun <D, P : RProps> dataLoadProps(query: suspend () -> D, toProps: (ReloadFunction, D) -> P) =
    DataLoadProps { reload -> query().let { result -> toProps(reload, result) } }