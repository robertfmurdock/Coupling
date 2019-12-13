package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.fitty.fitty
import com.zegreatrob.coupling.client.gravatar.GravatarOptions
import com.zegreatrob.coupling.client.gravatar.gravatarImage
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.css.*
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Node
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.div
import react.dom.img
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv

object PlayerCard : RComponent<PlayerCardProps>(provider()), PlayerCardBuilder

val RBuilder.playerCard get() = PlayerCard.render(this)

external interface PlayerCardStyles {
    val player: String
    val header: String
    val playerIcon: String
    val deselected: String
}

data class PlayerCardProps(
    val tribeId: TribeId,
    val player: Player,
    val pathSetter: (String) -> Unit = {},
    val headerDisabled: Boolean = false,
    val className: String? = null,
    val size: Int = 100,
    val onClick: ((Event) -> Unit) = {},
    val deselected: Boolean = false
) : RProps

interface PlayerCardBuilder : StyledComponentRenderer<PlayerCardProps, PlayerCardStyles> {

    override val componentPath: String get() = "player/PlayerCard"

    override fun StyledRContext<PlayerCardProps, PlayerCardStyles>.render(): ReactElement {
        val (tribeId, player, pathSetter, disabled, _, size, onClick) = props
        return reactElement {
            styledDiv {
                attrs {
                    classes += additionalClasses()
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

    private fun StyledRContext<PlayerCardProps, PlayerCardStyles>.additionalClasses() =
        setOf(styles.player, props.className)
            .filterNotNull()
            .let {
                when {
                    props.deselected -> it + styles.deselected
                    else -> it
                }
            }

    private fun StyledDOMBuilder<DIV>.playerCardStyle(size: Int) {
        css {
            width = size.px
            height = (size * 1.4).px
            padding(all = (size * 0.06).px)
            borderWidth = (size * 0.04).px
            borderRadius = (size * 0.08).px
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
        pathSetter: (String) -> Unit
    ) = { event: Event ->
        if (!disabled) {
            event.stopPropagation()

            pathSetter("/${tribeId.value}/player/${player.id}/")
        }
    }

    private fun Node.fitPlayerName(size: Int) {
        val maxFontHeight = (size * 0.31)
        val minFontHeight = (size * 0.16)
        fitty(maxFontHeight, minFontHeight, true)
    }

}
