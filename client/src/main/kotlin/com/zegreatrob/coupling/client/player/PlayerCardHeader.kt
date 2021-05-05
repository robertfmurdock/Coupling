package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.Paths.playerConfigPage
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.fitty.fitty
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import kotlinx.css.margin
import kotlinx.css.px
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Node
import org.w3c.dom.events.Event
import react.*
import react.dom.div
import react.router.dom.redirect
import styled.css
import styled.styledDiv

private val styles = useStyles("player/PlayerCard")

fun RBuilder.playerCardHeader(tribeId: TribeId, player: Player, size: Int, linkToConfig: Boolean) =
    child(playerCardHeader, PlayerCardHeaderProps(tribeId, player, linkToConfig, size))

data class PlayerCardHeaderProps(
    val tribeId: TribeId,
    val player: Player,
    val linkToConfig: Boolean,
    val size: Int
) : RProps

private val playerCardHeader = reactFunction<PlayerCardHeaderProps> { props ->
    val (tribeId, player, linkToConfig, size) = props
    val (redirectUrl, setRedirectUrl) = useState<String?>(null)

    val playerNameRef = useRef<Node?>(null)
    useLayoutEffect { playerNameRef.current?.fitPlayerName(size) }

    val nameClickHandler = if (linkToConfig) { event: Event ->
        event.stopPropagation()
        setRedirectUrl(tribeId.with(player).playerConfigPage())
    } else ({})

    if (redirectUrl != null)
        redirect(to = redirectUrl)
    else
        styledDiv {
            attrs {
                classes += styles["header"]
                onClickFunction = nameClickHandler
            }
            css { margin(top = (size * 0.02).px) }
            div {
                attrs { ref = playerNameRef }
                +(if (player.name.isBlank()) "Unknown" else player.name)
            }
        }
}

private fun Node.fitPlayerName(size: Int) {
    val maxFontHeight = (size * 0.31)
    val minFontHeight = (size * 0.16)
    fitty(maxFontHeight, minFontHeight, true)
}