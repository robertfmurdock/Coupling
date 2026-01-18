package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.components.Frame
import com.zegreatrob.coupling.client.components.FrameRunner
import js.objects.Record
import js.objects.unsafeJso
import react.FC
import react.dom.html.ReactHTML.button
import tanstack.react.router.useNavigate
import tanstack.react.router.useSearch
import tanstack.router.core.UseNavigateResult
import web.cssom.Angle
import web.cssom.Left
import web.cssom.Top
import web.cssom.deg
import web.cssom.px

private val loadingSequence by lazy { LoadingAnimationState.generateSequence() }

val LoadingPage = FC {
    val search = useSearch()
    val navigate = useNavigate()
    val frameIndex = search["frame"]?.toString()?.toIntOrNull()
    val currentFrame = frameIndex?.let { loadingSequence.toList()[it] }
    if (currentFrame != null) {
        button {
            onClick = { setFrame(navigate, frameIndex - 1) }
            +"<"
        }
        button {
            onClick = { setFrame(navigate, frameIndex + 1) }
            +">"
        }
        LoadingPageFrame(currentFrame.data)
    } else {
        FrameRunner(loadingSequence, 1.0, LoadingPageFrame::create)
    }
}

private fun setFrame(navigate: UseNavigateResult, frame: Int) {
    navigate(
        unsafeJso {
            search = fun(params: Record<String, String?>): Record<String, String?> {
                params["frame"] = "$frame"
                return params
            }.asDynamic()
        },
    )
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
    data object Initial : LoadingAnimationState(
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

    data object Step1 : LoadingAnimationState(
        LoadingAnimationStateData(
            player1Tilt = (-4).deg,
            player1Top = (-20).px,
            player1Left = 20.px,
            player2Tilt = 4.deg,
            player2Top = 20.px,
            player2Left = (-20).px,
            swapped = false,
        ),
    )

    data object Step2 : LoadingAnimationState(
        LoadingAnimationStateData(
            player1Tilt = 0.deg,
            player1Top = (-35).px,
            player1Left = 35.px,
            player2Tilt = 0.deg,
            player2Top = 35.px,
            player2Left = (-35).px,
            swapped = false,
        ),
    )

    data object Step3 : LoadingAnimationState(
        LoadingAnimationStateData(
            player1Tilt = 4.deg,
            player1Top = (-10).px,
            player1Left = 60.px,
            player2Tilt = (-4).deg,
            player2Top = 10.px,
            player2Left = (-60).px,
            swapped = false,
        ),
    )

    data object Swapped : LoadingAnimationState(
        LoadingAnimationStateData(
            player1Tilt = 8.deg,
            player1Top = 0.px,
            player1Left = 0.px,
            player2Tilt = (-8).deg,
            player2Top = 0.px,
            player2Left = 0.px,
            swapped = true,
        ),
    )

    data object Step5 : LoadingAnimationState(
        LoadingAnimationStateData(
            player2Tilt = (-4).deg,
            player2Top = (-20).px,
            player2Left = 20.px,
            player1Tilt = 4.deg,
            player1Top = 20.px,
            player1Left = (-20).px,
            swapped = true,
        ),
    )

    data object Step6 : LoadingAnimationState(
        LoadingAnimationStateData(
            player1Tilt = 0.deg,
            player1Top = 35.px,
            player1Left = (-35).px,
            player2Tilt = 0.deg,
            player2Top = (-35).px,
            player2Left = 35.px,
            swapped = true,
        ),
    )

    data object Step7 : LoadingAnimationState(
        LoadingAnimationStateData(
            player1Tilt = (-4).deg,
            player1Top = 10.px,
            player1Left = (-60).px,
            player2Tilt = 4.deg,
            player2Top = (-10).px,
            player2Left = 60.px,
            swapped = true,
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
            Pair(Step1, 100),
            Pair(Step2, 100),
            Pair(Step3, 100),
            Pair(Swapped, 100),
            Pair(Swapped, 100),
            Pair(Swapped, 100),
            Pair(Swapped, 100),
            Pair(Step5, 100),
            Pair(Step6, 100),
            Pair(Step7, 100),
            Pair(Initial, 100),
            Pair(Initial, 100),
            Pair(Initial, 100),
            Pair(Initial, 100),
        )
    }
}
