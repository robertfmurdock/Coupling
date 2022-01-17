package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.Paths.playerConfigPage
import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.fitty.fitty
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.tmFC
import kotlinx.css.*
import kotlinx.html.classes
import org.w3c.dom.Node
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.ref
import react.router.dom.Link
import react.useLayoutEffect
import react.useRef

private val styles = useStyles("player/PlayerCard")

data class PlayerCardHeader(
    val tribeId: TribeId,
    val player: Player,
    val linkToConfig: Boolean,
    val size: Int
) : DataProps<PlayerCardHeader> {
    override val component: TMFC<PlayerCardHeader> get() = playerCardHeader
}

private val playerCardHeader = tmFC<PlayerCardHeader> { props ->
    val (tribeId, player, linkToConfig, size) = props
    val playerNameRef = useRef<Node>(null)
    useLayoutEffect { playerNameRef.current?.fitPlayerName(size) }
    cssDiv(attrs = { classes = setOf(styles["header"]) },
        props = { ref = playerNameRef },
        css = {
            margin(top = (size * 0.02).px)
            height = (size * 0.33).px
            overflow = Overflow.hidden
        }) {
        cssDiv(css = {
            display = Display.flex
            alignItems = Align.center
            height = (size * 0.33).px
        }) {
            optionalLink(shouldLink = linkToConfig, url = tribeId.with(player).playerConfigPage()) {
                div {
                    +(player.name.ifBlank { "Unknown" })
                }
            }
        }
    }
}

private fun ChildrenBuilder.optionalLink(
    shouldLink: Boolean,
    url: String,
    handler: ChildrenBuilder.() -> Unit
) = if (shouldLink)
    Link { to = url; handler() }
else
    handler()

private fun Node.fitPlayerName(size: Int) = fitty(
    maxFontHeight = (size * 0.31),
    minFontHeight = (size * 0.10),
    multiLine = true
)
