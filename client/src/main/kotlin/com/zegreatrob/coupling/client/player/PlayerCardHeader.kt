package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.PathSetter
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.fitty.fitty
import com.zegreatrob.coupling.client.playerConfig
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import kotlinx.css.margin
import kotlinx.css.px
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Node
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.dom.div
import react.useLayoutEffect
import react.useRef
import styled.css
import styled.styledDiv

private val styles = useStyles("player/PlayerCard")

fun RBuilder.playerCardHeader(tribeId: TribeId, player: Player, size: Int, pathSetter: PathSetter?) =
    child(playerCardHeader, PlayerCardHeaderProps(tribeId, player, pathSetter, size))

data class PlayerCardHeaderProps(
    val tribeId: TribeId,
    val player: Player,
    val pathSetter: PathSetter?,
    val size: Int
) : RProps

private val playerCardHeader = reactFunction<PlayerCardHeaderProps> { props ->
    val (tribeId, player, pathSetter, size) = props
    val playerNameRef = useRef<Node?>(null)
    useLayoutEffect { playerNameRef.current?.fitPlayerName(size) }

    styledDiv {
        attrs {
            classes += styles["header"]
            onClickFunction = handleNameClick(tribeId, player, pathSetter)
        }
        css { margin(top = (size * 0.02).px) }
        div {
            attrs { ref = playerNameRef }
            +(if (player.name.isBlank()) "Unknown" else player.name)
        }
    }
}

private fun handleNameClick(tribeId: TribeId, player: Player, pathSetter: PathSetter?) =
    { event: Event ->
        if (pathSetter != null) {
            event.stopPropagation()

            pathSetter.playerConfig(tribeId, player)
        }
    }

private fun Node.fitPlayerName(size: Int) {
    val maxFontHeight = (size * 0.31)
    val minFontHeight = (size * 0.16)
    fitty(maxFontHeight, minFontHeight, true)
}