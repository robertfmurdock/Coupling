package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.Paths.playerConfigPage
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.fitty.fitty
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import kotlinx.css.margin
import kotlinx.css.px
import kotlinx.html.classes
import org.w3c.dom.Node
import react.ReactElement
import react.create
import react.dom.attrs
import react.dom.html.ReactHTML.div
import react.router.dom.Link
import react.useLayoutEffect
import react.useRef
import styled.css
import styled.styledDiv

private val styles = useStyles("player/PlayerCard")

data class PlayerCardHeader(
    val tribeId: TribeId,
    val player: Player,
    val linkToConfig: Boolean,
    val size: Int
) : DataProps<PlayerCardHeader> {
    override val component: TMFC<PlayerCardHeader> get() = playerCardHeader
}

private val playerCardHeader = reactFunction<PlayerCardHeader> { props ->
    val (tribeId, player, linkToConfig, size) = props
    val playerNameRef = useRef<Node>(null)
    useLayoutEffect { playerNameRef.current?.fitPlayerName(size) }
    styledDiv {
        attrs { classes = setOf(styles["header"]) }
        css { margin(top = (size * 0.02).px) }
        +optionalLink(shouldLink = linkToConfig, url = tribeId.with(player).playerConfigPage()) {
            div.create {
                ref = playerNameRef
                +(player.name.ifBlank { "Unknown" })
            }
        }
    }
}

private fun optionalLink(
    shouldLink: Boolean,
    url: String,
    handler: () -> ReactElement
): ReactElement = if (shouldLink)
    Link.create {
        to = url
        child(handler())
    }
else
    handler()

private fun Node.fitPlayerName(size: Int) {
    val maxFontHeight = (size * 0.31)
    val minFontHeight = (size * 0.16)
    fitty(maxFontHeight, minFontHeight, true)
}