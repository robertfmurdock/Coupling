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
import react.RReadableRef
import react.dom.div
import react.dom.img
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv

@JsModule("react")
@JsNonModule
private external val React: dynamic

private external interface Styles {
    var player: String
    var header: String
}

private val styles: Styles = loadStyles("PlayerCard").unsafeCast<Styles>()

fun RBuilder.playerCard(
        tribeId: String,
        player: Player,
        disabled: Boolean = false,
        className: String?,
        size: Int = 100,
        onClick: (Event) -> Unit,
        pathSetter: (String) -> Unit
) {
    styledDiv {
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

fun RBuilder.playerGravatarImage(player: Player, size: Int) {
    if (player.imageURL != null) {
        img(
                src = player.imageURL,
                classes = "player-icon",
                alt = "icon"
        ) {
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
}

fun RBuilder.playerCardHeader(
        tribeId: String,
        player: Player,
        size: Int,
        disabled: Boolean,
        pathSetter: (String) -> Unit
) {
    val playerNameRef: RReadableRef<Node> = React.useRef(null).unsafeCast<RReadableRef<Node>>()

    React.useLayoutEffect {
        playerNameRef.current?.fitPlayerName(size)
        return@useLayoutEffect undefined
    }

    styledDiv {
        attrs {
            classes = setOf("player-card-header", styles.header)
            onClickFunction = handleNameClick(tribeId, player, disabled, pathSetter)
        }
        css {
            margin(top = (size * 0.02).px)
        }
        div {
            attrs {
                ref = playerNameRef
            }
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

private fun StyledDOMBuilder<DIV>.playerCardStyle(size: Int) {
    css {
        width = size.px
        height = (size * 1.4).px
        padding(all = (size * 0.06).px)
        borderWidth = (size * 0.01).px
    }
}