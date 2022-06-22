package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.CardHeader
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.components.pngPath
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.tmFC
import csstype.BackgroundRepeat
import csstype.Color
import csstype.Display
import csstype.FontWeight
import csstype.Globals
import csstype.LineStyle
import csstype.None
import csstype.TransitionProperty
import csstype.TransitionTimingFunction
import csstype.px
import csstype.rgba
import csstype.s
import csstype.url
import emotion.react.css

private val styles = useStyles("player/PlayerCard")

data class PlayerCardHeader(val player: Player, val size: Int) : DataPropsBind<PlayerCardHeader>(playerCardHeader)

private val playerCardHeader = tmFC<PlayerCardHeader> { props ->
    val (player, size) = props
    CardHeader {
        this.size = size
        css(styles["header"]) {
            backgroundColor = rgba(255, 255, 255, 0.4)
            backgroundImage = url(pngPath("overlay"))
            backgroundRepeat = BackgroundRepeat.repeatX
            borderStyle = LineStyle.solid
            borderColor = Color("#00000054")
            borderWidth = 1.px
            fontWeight = FontWeight.bold

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
