package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.components.external.reactfliptoolkit.Flipped
import com.zegreatrob.coupling.client.components.external.reactfliptoolkit.Flipper
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.client.components.welcome.playerImage
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.css.ClassName
import emotion.react.css
import react.Props
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
import web.cssom.pct
import web.cssom.translate

external interface LoadingPageFrameProps : Props {
    var state: LoadingAnimationState
}

@ReactFunc
val LoadingPageFrame by nfc<LoadingPageFrameProps> { (state) ->
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
                player1 = defaultPlayer.copy(id = "rob", name = "rob", imageURL = rob),
                player2 = defaultPlayer.copy(id = "autumn", name = "autumn", imageURL = autumn),
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
                        }.forEach { +it }
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
        PlayerCard(
            player = player,
            className = leftCardStyles,
            size = 50,
            tilt = tilt,
        )
    }
}
