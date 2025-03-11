package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.components.external.reactfliptoolkit.Flipped
import com.zegreatrob.coupling.client.components.external.reactfliptoolkit.Flipper
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.client.components.welcome.playerImage
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
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
                player1 = defaultPlayer.copy(id = PlayerId.new(), name = "rob", imageURL = rob),
                player2 = defaultPlayer.copy(id = PlayerId.new(), name = "autumn", imageURL = autumn),
            )
            val data = state.data
            Flipper {
                flipKey = "$data"
                div {
                    div {
                        css {
                            display = Display.inlineFlex
                        }
                        listOf(
                            flippedPlayer(
                                ClassName { },
                                pair.player1,
                                data.player1Top,
                                data.player1Tilt,
                                data.player1Left,
                            ),
                            flippedPlayer(
                                ClassName { },
                                pair.player2,
                                data.player2Top,
                                data.player2Tilt,
                                data.player2Left,
                            ),
                        ).let {
                            if (data.swapped) {
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
    flipId = player.id.value.toString()
    key = player.id.value.toString()
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
