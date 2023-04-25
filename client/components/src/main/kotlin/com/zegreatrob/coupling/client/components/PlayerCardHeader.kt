package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.ntmFC
import emotion.react.css
import web.cssom.Display
import web.cssom.Globals
import web.cssom.None
import web.cssom.TransitionProperty
import web.cssom.TransitionTimingFunction
import web.cssom.s

data class PlayerCardHeader(val player: Player, val size: Int) : DataPropsBind<PlayerCardHeader>(playerCardHeader)

private val playerCardHeader by ntmFC<PlayerCardHeader> { props ->
    val (player, size) = props
    CardHeader {
        this.size = size
        css {
            transitionProperty = TransitionProperty.all
            transitionTimingFunction = TransitionTimingFunction.easeOut
            transitionDuration = 0.4.s

            "a" {
                color = Globals.inherit
                textDecoration = None.none
                display = Display.block
            }
        }
        this.headerContent = player.name
    }
}
