package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.Paths.playerConfigPage
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.fitty.fitty
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.minreact.reactFunction
import kotlinx.css.margin
import kotlinx.css.px
import kotlinx.html.classes
import org.w3c.dom.Node
import react.Props
import react.RBuilder
import react.dom.attrs
import react.dom.div
import react.router.dom.Link
import react.useLayoutEffect
import react.useRef
import styled.css
import styled.styledDiv

private val styles = useStyles("player/PlayerCard")

fun RBuilder.playerCardHeader(tribeId: TribeId, player: Player, size: Int, linkToConfig: Boolean) =
    child(playerCardHeader, PlayerCardHeadeProps(tribeId, player, linkToConfig, size))

data class PlayerCardHeadeProps(
    val tribeId: TribeId,
    val player: Player,
    val linkToConfig: Boolean,
    val size: Int
) : Props

private val playerCardHeader = reactFunction<PlayerCardHeadeProps> { props ->
    val (tribeId, player, linkToConfig, size) = props
    val playerNameRef = useRef<Node>(null)
    useLayoutEffect { playerNameRef.current?.fitPlayerName(size) }
    styledDiv {
        attrs { classes = setOf(styles["header"]) }
        css { margin(top = (size * 0.02).px) }
        optionalLink(shouldLink = linkToConfig, url = tribeId.with(player).playerConfigPage()) {
            div {
                attrs { ref = playerNameRef }
                +(player.name.ifBlank { "Unknown" })
            }
        }
    }
}

private fun RBuilder.optionalLink(
    shouldLink: Boolean,
    url: String,
    handler: RBuilder.() -> Unit
) {
    if (shouldLink)
        Link {
            attrs.to = url
            handler()
        }
    else
        handler()
}

private fun Node.fitPlayerName(size: Int) {
    val maxFontHeight = (size * 0.31)
    val minFontHeight = (size * 0.16)
    fitty(maxFontHeight, minFontHeight, true)
}