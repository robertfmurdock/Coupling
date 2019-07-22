package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.css.*
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Node
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.dom.div
import react.dom.img
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv

object PlayerCard : ComponentProvider<PlayerCardProps>(), PlayerCardBuilder

val RBuilder.playerCard get() = PlayerCard.captor(this)

external interface PlayerCardStyles {
    val player: String
    val header: String
    val playerIcon: String
}

data class PlayerCardProps(
        val tribeId: TribeId,
        val player: Player,
        val pathSetter: (String) -> Unit,
        val disabled: Boolean = false,
        val className: String? = null,
        val size: Int = 100,
        val onClick: ((Event) -> Unit) = {}
) : RProps

interface PlayerCardBuilder : StyledComponentBuilder<PlayerCardProps, PlayerCardStyles> {

    override val componentPath: String get() = "player/PlayerCard"

    override fun build() = buildBy {
        val (tribeId, player, pathSetter, disabled, className, size, onClick) = props
        {
            styledDiv {
                attrs {
                    classes += setOf(styles.player, className).filterNotNull()
                    playerCardStyle(size)
                    onClickFunction = onClick
                }
                playerGravatarImage(player, size, styles)
                playerCardHeader(
                        tribeId = tribeId,
                        player = player,
                        size = size,
                        disabled = disabled,
                        pathSetter = pathSetter,
                        styles = styles
                )
            }
        }
    }

    private fun StyledDOMBuilder<DIV>.playerCardStyle(size: Int) {
        css {
            width = size.px
            height = (size * 1.4).px
            padding(all = (size * 0.06).px)
            borderWidth = (size * 0.01).px
        }
    }

    private fun RBuilder.playerGravatarImage(
            player: Player,
            size: Int,
            styles: PlayerCardStyles
    ) = if (player.imageURL != null) {
        img(src = player.imageURL, classes = styles.playerIcon, alt = "icon") {
            attrs {
                width = size.toString()
                height = size.toString()
            }
        }
    } else {
        val email = player.email ?: player.name ?: ""
        gravatarImage(
                email = email,
                className = styles.playerIcon,
                alt = "icon",
                options = object : GravatarOptions {
                    override val size = size
                    override val default = "retro"
                }
        )
    }

    private fun RBuilder.playerCardHeader(
            tribeId: TribeId,
            player: Player,
            size: Int,
            disabled: Boolean,
            pathSetter: (String) -> Unit,
            styles: PlayerCardStyles
    ) {
        val playerNameRef = useRef<Node>(null)
        useLayoutEffect { playerNameRef.current?.fitPlayerName(size) }

        styledDiv {
            attrs {
                classes += styles.header
                onClickFunction = handleNameClick(tribeId, player, disabled, pathSetter)
            }
            css {
                margin(top = (size * 0.02).px)
            }
            div {
                attrs { ref = playerNameRef }
                +(if (player.id == null) "NEW:" else "")
                +(if (player.name.isNullOrBlank()) "Unknown" else player.name!!)
            }
        }
    }

    private fun handleNameClick(
            tribeId: TribeId,
            player: Player,
            disabled: Boolean,
            pathSetter: (String) -> Unit) = { event: Event ->
        if (!disabled) {
            event.stopPropagation()

            pathSetter("/${tribeId.value}/player/${player.id}/")
        }
    }

    private fun Node.fitPlayerName(size: Int) {
        val maxFontHeight = (size * 0.31)
        val minFontHeight = (size * 0.16)
        fitHeaderNode(maxFontHeight, minFontHeight)
    }

}
