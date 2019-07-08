package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.common.entity.player.Player
import kotlinx.css.*
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import loadStyles
import org.w3c.dom.Node
import org.w3c.dom.events.Event
import react.*
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

interface PlayerCardProps : RProps {
    var tribeId: String
    var player: Player
    var disabled: Boolean?
    var className: String?
    var size: Int?
    var onClick: ((Event) -> Unit)?
    var pathSetter: (String) -> Unit
}

val playerCard = { props: PlayerCardProps ->
    buildElement {
        playerCard(
                props.tribeId,
                props.player,
                props.disabled ?: false,
                props.className,
                props.size ?: 100,
                props.onClick ?: {},
                props.pathSetter
        )
    }
}.unsafeCast<RClass<PlayerCardProps>>()

private fun RBuilder.playerCard(
        tribeId: String,
        player: Player,
        disabled: Boolean,
        className: String?,
        size: Int,
        onClick: (Event) -> Unit = {},
        pathSetter: (String) -> Unit
): ReactElement {
    return styledDiv {
        attrs {
            classes += setOf(
                    styles.player,
                    "react-player-card",
                    className
            ).filterNotNull()
            playerCardStyle(size)
            onClickFunction = onClick
        }
        playerGravatarImage(player, size)
        playerCardHeader(tribeId, player, size, disabled, pathSetter)
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

