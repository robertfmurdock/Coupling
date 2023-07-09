package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.components.Frame
import com.zegreatrob.coupling.client.components.FrameRunner
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.minreact.nfc
import web.cssom.Angle
import web.cssom.Left
import web.cssom.Top
import web.cssom.deg
import web.cssom.px

val loadingSequence by lazy { LoadingAnimationState.generateSequence() }
val LoadingPage by nfc<PageProps> { props ->
    val frameIndex = props.search["frame"]
    val currentFrame = frameIndex?.toIntOrNull()?.let { loadingSequence.toList()[it] }
    if (currentFrame != null) {
        LoadingPageFrame(currentFrame.data)
    } else {
        FrameRunner(loadingSequence, 1.0, { state: LoadingAnimationState -> LoadingPageFrame.create(state) })
    }
}

data class LoadingAnimationStateData(
    val player1Tilt: Angle,
    val player1Top: Top,
    val player1Left: Left,
    val player2Tilt: Angle,
    val player2Top: Top,
    val swapped: Boolean,
    val player2Left: Left,
)

sealed class LoadingAnimationState(
    val data: LoadingAnimationStateData,
) {
    object Initial : LoadingAnimationState(
        LoadingAnimationStateData(
            player1Tilt = (-8).deg,
            player1Top = 0.px,
            player1Left = 0.px,
            player2Tilt = 8.deg,
            player2Top = 0.px,
            swapped = false,
            player2Left = 0.px,
        ),
    )

    object Step1 : LoadingAnimationState(
        LoadingAnimationStateData(
            player1Tilt = (8).deg,
            player1Top = (-25).px,
            player1Left = 25.px,
            player2Tilt = (-8).deg,
            player2Top = 25.px,
            player2Left = (-25).px,
            swapped = false,
        ),
    )

    object Swapped : LoadingAnimationState(
        LoadingAnimationStateData(
            player1Tilt = 8.deg,
            player1Top = 0.px,
            player1Left = 0.px,
            player2Tilt = (-8).deg,
            player2Top = 0.px,
            swapped = true,
            player2Left = 0.px,
        ),
    )

    companion object {
        fun generateSequence(): Sequence<Frame<LoadingAnimationState>> = (1..100)
            .map { frames }
            .flatten()
            .runningFold(Frame<LoadingAnimationState>(Initial, 0)) { frame, (state, time) ->
                Frame(state, frame.delay + time)
            }.asSequence()

        private val frames = listOf(
            Pair(Step1, 1000),
            Pair(Swapped, 300),
            Pair(Initial, 1000),
        )
    }
}
