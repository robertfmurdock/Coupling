package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.animationsDisabledContext
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.react.dataloader.DataLoadState
import com.zegreatrob.react.dataloader.EmptyState
import react.FC
import react.PropsWithChildren
import react.create
import react.dom.html.ReactHTML.div
import react.useState

private val styles = useStyles("routing/DataLoadWrapper")

external interface AnimationFrameProps : PropsWithChildren {
    var state: DataLoadState<*>
}

enum class AnimationState {
    Start, Stop
}

val animationFrame = FC<AnimationFrameProps> { props ->
    var animationState by useState(AnimationState.Start)
    val shouldStartAnimation = props.state !is EmptyState && animationState === AnimationState.Start

    animationsDisabledContext.Consumer {
        children = { animationsDisabled: Boolean ->
            div.create {
                className = styles["viewFrame"]
                if (shouldStartAnimation && !animationsDisabled) {
                    className = listOf(className, "ng-enter").joinToString(" ")
                }
                onAnimationEnd = { animationState = AnimationState.Stop }
                +props.children
            }
        }
    }
}
