package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.FrameRunner
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.minreact.add
import react.FC

val styles = useStyles("DemoPage")

val demoSequence by lazy { DemoAnimationState.generateSequence() }

val DemoPage = FC<PageProps> { props ->
    val frameIndex = props.search.get("frame")
    val currentFrame = frameIndex?.toIntOrNull()?.let { demoSequence.toList()[it] }
    if (currentFrame != null) {
        add(DemoPageFrame(currentFrame.data))
    } else {
        add(
            FrameRunner(demoSequence, 1.0) { state: DemoAnimationState ->
                add(DemoPageFrame(state))
            }
        )
    }
}
