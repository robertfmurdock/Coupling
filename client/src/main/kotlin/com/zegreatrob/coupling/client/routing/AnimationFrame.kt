package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minreact.reactFunction
import com.zegreatrob.react.dataloader.DataLoadState
import com.zegreatrob.react.dataloader.EmptyState
import kotlinx.html.classes
import react.RProps
import react.dom.attrs
import react.dom.div
import react.useState

private val styles = useStyles("routing/DataLoadWrapper")

data class AnimationFrameProps<D>(val state: DataLoadState<D>) : RProps

enum class AnimationState {
    Start, Stop
}

val animationFrame = reactFunction<AnimationFrameProps<*>> { props ->
    var animationState by useState(AnimationState.Start)
    val shouldStartAnimation = props.state !is EmptyState && animationState === AnimationState.Start

    animationsDisabledContext.Consumer { animationsDisabled: Boolean ->
        div {
            attrs {
                classes = classes + styles["viewFrame"]
                if (shouldStartAnimation && !animationsDisabled) {
                    classes = classes + "ng-enter"
                }
                this["onAnimationEnd"] = { animationState = AnimationState.Stop }
            }
            props.children()
        }
    }
}
