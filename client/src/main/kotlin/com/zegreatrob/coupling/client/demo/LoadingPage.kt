package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.components.Frame
import com.zegreatrob.coupling.client.components.FrameRunner
import com.zegreatrob.coupling.client.components.PlayerCard
import com.zegreatrob.coupling.client.components.external.reactfliptoolkit.Flipped
import com.zegreatrob.coupling.client.components.external.reactfliptoolkit.Flipper
import com.zegreatrob.coupling.client.components.welcome.playerImage
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import com.zegreatrob.minreact.ntmFC
import emotion.css.ClassName
import emotion.react.css
import react.create
import react.dom.html.ReactHTML.div
import web.cssom.AlignItems
import web.cssom.Angle
import web.cssom.ClassName
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.Left
import web.cssom.Position
import web.cssom.Top
import web.cssom.deg
import web.cssom.pct
import web.cssom.px
import web.cssom.translate

val loadingSequence by lazy { LoadingAnimationState.generateSequence() }
val LoadingPage by nfc<PageProps> { props ->
    val frameIndex = props.search["frame"]
    val currentFrame = frameIndex?.toIntOrNull()?.let { loadingSequence.toList()[it] }
    if (currentFrame != null) {
        add(LoadingPageFrame(currentFrame.data))
    } else {
        add(
            FrameRunner(loadingSequence, 1.0) { state: LoadingAnimationState ->
                add(LoadingPageFrame(state))
            },
        )
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

data class LoadingPageFrame(val state: LoadingAnimationState) : DataPropsBind<LoadingPageFrame>(loadingPageFrame)

private val loadingPageFrame by ntmFC<LoadingPageFrame> { (state) ->
    div {
        css {
            display = Display.flex
            justifyContent = JustifyContent.center
            alignItems = AlignItems.center
        }
        div {
            css {
                position = Position.absolute
                top = 50.pct
                left = 50.pct
                transform = translate((-50).pct, (-50).pct)
            }
            val rob by playerImage()
            val autumn by playerImage()
            val pair = pairOf(
                player1 = Player(id = "rob", name = "rob", imageURL = rob, avatarType = null),
                player2 = Player(id = "autumn", name = "autumn", imageURL = autumn, avatarType = null),
            )
            Flipper {
                flipKey = "${state.data}"
                div {
                    div {
                        css {
                            display = Display.inlineFlex
                        }
                        listOf(
                            flippedPlayer(
                                ClassName {},
                                pair.player1,
                                state.data.player1Top,
                                state.data.player1Tilt,
                                state.data.player1Left,
                            ),
                            flippedPlayer(
                                ClassName {},
                                pair.player2,
                                state.data.player2Top,
                                state.data.player2Tilt,
                                state.data.player2Left,
                            ),
                        ).let {
                            if (state.data.swapped) {
                                it.reversed()
                            } else {
                                it
                            }
                        }.forEach { child(it) }
                    }
                }
            }
        }
    }
}

private fun flippedPlayer(
    leftCardStyles: ClassName,
    player: Player,
    top: Top,
    tilt: Angle,
    left: Left,
) = Flipped.create {
    flipId = player.id
    div {
        css {
            position = Position.relative
            this@css.top = top
            this@css.left = left
        }
        add(
            PlayerCard(
                player = player,
                className = leftCardStyles,
                size = 50,
                tilt = tilt,
            ),
        )
    }
}
