package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.common.entity.player.Player
import kotlinx.css.*
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import loadStyles
import org.w3c.dom.Node
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.dom.div
import react.dom.img
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv

private external interface Styles {
    var player: String
    var header: String
}

private val styles: Styles = loadStyles("PlayerCard")

external interface PlayerCardProps : RProps {
    var tribeId: String
    var player: Player
    var disabled: Boolean?
    var className: String?
    var size: Int?
    var onClick: ((Event) -> Unit)?
    var pathSetter: (String) -> Unit
}

fun PlayerCardProps.getDisabled(): Boolean = disabled ?: false
fun PlayerCardProps.getSize(): Int = size ?: 100
fun PlayerCardProps.getOnClick(): (Event) -> Unit = onClick ?: {}

val playerCard = rFunction { props: PlayerCardProps ->
    styledDiv {
        attrs {
            classes += setOf(
                    styles.player,
                    "react-player-card",
                    props.className
            ).filterNotNull()
            playerCardStyle(props.getSize())
            onClickFunction = props.getOnClick()
        }
        playerGravatarImage(props.player, props.getSize())
        playerCardHeader(
                tribeId = props.tribeId,
                player = props.player,
                size = props.getSize(),
                disabled = props.getDisabled(),
                pathSetter = props.pathSetter
        )
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

fun RBuilder.playerGravatarImage(player: Player, size: Int) = if (player.imageURL != null) {
    img(src = player.imageURL, classes = "player-icon", alt = "icon") {
        attrs {
            width = size.toString()
            height = size.toString()
        }
    }
} else {
    val email = player.email ?: player.name ?: ""
    gravatarImage(
            email = email,
            className = "player-icon",
            alt = "icon",
            options = object : GravatarOptions {
                override val size = size
                override val default = "retro"
            }
    )
}

fun RBuilder.playerCardHeader(
        tribeId: String,
        player: Player,
        size: Int,
        disabled: Boolean,
        pathSetter: (String) -> Unit
) {
    val playerNameRef = useRef(null)
    useLayoutEffect { playerNameRef.current?.fitPlayerName(size) }

    styledDiv {
        attrs {
            classes = setOf("player-card-header", styles.header)
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
        tribeId: String,
        player: Player,
        disabled: Boolean,
        pathSetter: (String) -> Unit) = { event: Event ->
    if (!disabled) {
        event.stopPropagation()

        pathSetter("/$tribeId/player/${player.id}/")
    }
}

private fun Node.fitPlayerName(size: Int) {
    val maxFontHeight = (size * 0.31)
    val minFontHeight = (size * 0.16)
    fitHeaderNode(maxFontHeight, minFontHeight)
}

