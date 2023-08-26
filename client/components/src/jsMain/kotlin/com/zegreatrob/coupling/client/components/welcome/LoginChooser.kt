package com.zegreatrob.coupling.client.components.welcome

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.DemoButton
import com.zegreatrob.coupling.client.components.external.auth0.react.RedirectLoginOptions
import com.zegreatrob.coupling.client.components.external.auth0.react.useAuth0Data
import com.zegreatrob.coupling.client.components.pngPath
import com.zegreatrob.coupling.client.components.supersize
import com.zegreatrob.coupling.client.components.white
import com.zegreatrob.minreact.nfc
import emotion.react.css
import js.core.jso
import react.Props
import react.dom.html.ReactHTML.div
import react.router.dom.useSearchParams
import web.cssom.BackgroundRepeat
import web.cssom.Border
import web.cssom.BoxShadow
import web.cssom.Clear
import web.cssom.Color
import web.cssom.Display
import web.cssom.LineStyle
import web.cssom.Margin
import web.cssom.NamedColor
import web.cssom.None
import web.cssom.Overflow
import web.cssom.Padding
import web.cssom.Position
import web.cssom.TextAlign
import web.cssom.px
import web.cssom.rgb
import web.cssom.url
import web.window.window

val LoginChooser by nfc<Props> {
    val auth0Data = useAuth0Data()
    val (params) = useSearchParams()
    val returnPath = params["path"] ?: ""
    val signInFunc = {
        auth0Data.loginWithRedirect(
            RedirectLoginOptions(
                appState = jso { returnTo = "${window.asDynamic()["basename"]}$returnPath" },
            ),
        )
    }
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
            boxShadow = BoxShadow(1.px, 2.px, 2.px, rgb(0, 0, 0, 0.6))
            color = NamedColor.black
            margin = Margin(0.px, 2.px, 0.px, 2.px)
            top = 0.px
        }
        div {
            CouplingButton(
                sizeRuleSet = supersize,
                colorRuleSet = white,
                onClick = signInFunc,
            ) { +"Login" }
        }
        div { DemoButton() }
    }
}
