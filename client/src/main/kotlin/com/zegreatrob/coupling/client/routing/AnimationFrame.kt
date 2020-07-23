package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minreact.reactFunction
import kotlinx.html.classes
import react.RProps
import react.dom.div
import react.useState

private val styles = useStyles("routing/DataLoadWrapper")

data class AnimationFrameProps<D>(val state: DataLoadState<D>) : RProps

enum class AnimationState {
    Start, Stop
}

val animationFrame = reactFunction<AnimationFrameProps<*>> { props ->
    val (animationState, setAnimationState) = useState(AnimationState.Start)
    val shouldStartAnimation = props.state !is EmptyState && animationState === AnimationState.Start

    animationsDisabledContext.Consumer { animationsDisabled: Boolean ->
        div {
            attrs {
                classes += styles["viewFrame"]
                if (shouldStartAnimation && !animationsDisabled) {
                    classes += "ng-enter"
                }
                this["onAnimationEnd"] = { setAnimationState(AnimationState.Stop) }
            }
            props.children()
        }
    }
}
