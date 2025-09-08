package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.client.components.CardHeader
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import web.cssom.Display
import web.cssom.Globals
import web.cssom.None
import web.cssom.TransitionProperty
import web.cssom.TransitionTimingFunction
import web.cssom.s

external interface PlayerCardHeaderProps : Props {
    var player: Player
    var size: Int
}

@ReactFunc
val PlayerCardHeader by nfc<PlayerCardHeaderProps> { props ->
    CardHeader {
        size = props.size
        css {
            transitionProperty = TransitionProperty.Companion.all
            transitionTimingFunction = TransitionTimingFunction.Companion.easeOut
            transitionDuration = 0.4.s

            "a" {
                color = Globals.Companion.inherit
                textDecoration = None.Companion.none
                display = Display.Companion.block
            }
        }
        headerContent = props.player.name
    }
}
