package com.zegreatrob.coupling.client.components.welcome

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.DemoButton
import com.zegreatrob.coupling.client.components.external.auth0.react.useAuth0Data
import com.zegreatrob.coupling.client.components.pngPath
import com.zegreatrob.coupling.client.components.supersize
import com.zegreatrob.coupling.client.components.white
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import csstype.BackgroundRepeat
import csstype.Border
import csstype.BoxShadow
import csstype.Clear
import csstype.Color
import csstype.Display
import csstype.LineStyle
import csstype.Margin
import csstype.NamedColor
import csstype.None
import csstype.Overflow
import csstype.Padding
import csstype.Position
import csstype.TextAlign
import csstype.px
import csstype.rgba
import csstype.url
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.div

val LoginChooser by nfc<Props> {
    val auth0Data = useAuth0Data()
    val signInFunc = { auth0Data.loginWithRedirect() }
    div {
        css {
            borderRadius = 82.px
            padding = Padding(18.px, 42.px)
            border = Border(24.px, LineStyle.solid, Color("#e22092"))
            position = Position.relative
            clear = Clear.both
            display = Display.block
            overflow = Overflow.hidden
            backgroundColor = Color("#ffccd8")
            backgroundImage = url(pngPath("overlay"))
            backgroundRepeat = BackgroundRepeat.repeatX
            textAlign = TextAlign.center
            textDecoration = None.none
            boxShadow = BoxShadow(1.px, 2.px, 2.px, rgba(0, 0, 0, 0.6))
            color = NamedColor.black
            margin = Margin(0.px, 2.px, 0.px, 2.px)
            top = 0.px
        }
        div {
            add(
                CouplingButton(
                    supersize,
                    white,
                    onClick = signInFunc,
                ),
            ) {
                +"Login"
            }
        }
        div { DemoButton() }
    }
}
