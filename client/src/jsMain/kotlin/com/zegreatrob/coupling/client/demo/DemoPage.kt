package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.components.FrameRunner
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.minreact.nfc

val demoSequence by lazy { DemoAnimationState.generateSequence() }

val DemoPage by nfc<PageProps> { props ->
    val frameIndex = props.search["frame"]
    val currentFrame = frameIndex?.toIntOrNull()?.let { demoSequence.toList()[it] }
    if (currentFrame != null) {
        DemoPageFrame(currentFrame.data)
    } else {
        FrameRunner(demoSequence, 1.0, DemoPageFrame::create)
    }
}
