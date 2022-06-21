package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.DemoButton
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.dom.white
import com.zegreatrob.coupling.client.external.auth0.react.useAuth0Data
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pngPath
import com.zegreatrob.minreact.add
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
import react.FC
import react.Props
import react.dom.html.ReactHTML.div

private val styles = useStyles("LoginChooser")

val LoginChooser = FC<Props> {
    val auth0Data = useAuth0Data()
    val signInFunc = { auth0Data.loginWithRedirect() }
    div {
        css(styles.className) {
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
            add(CouplingButton(supersize, white, styles["loginButton"], signInFunc)) {
                +"Login"
            }
        }
        div { DemoButton() }
    }
}
