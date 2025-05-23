package com.zegreatrob.coupling.client.incubating

import com.zegreatrob.minreact.nfc
import emotion.react.css
import js.objects.unsafeJso
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.svg.ReactSVG.path
import react.dom.svg.ReactSVG.svg
import web.cssom.AlignItems
import web.cssom.Color
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.LineStyle.Companion.solid
import web.cssom.None
import web.cssom.integer
import web.cssom.px
import web.cssom.rgb
import web.cssom.string

external interface AddToSlackButtonProps : Props {
    var url: String
}

val AddToSlackButton by nfc<AddToSlackButtonProps> { props ->
    a {
        href = props.url
        css {
            alignItems = AlignItems.center
            color = rgb(0, 0, 0)
            backgroundColor = Color("#fff")
            borderWidth = 1.px
            borderStyle = solid
            borderColor = Color("#ddd")
            borderRadius = 56.px
            display = Display.inlineFlex
            fontFamily = string("Lato, sans-serif")
            fontSize = 18.px
            fontWeight = integer(600)
            height = 56.px
            justifyContent = JustifyContent.center
            textDecoration = None.none
            width = 276.px
            hover {
                backgroundColor = Color("#f7f7f7")
            }
        }
        svg {
            xmlns = "http://www.w3.org/2000/svg"
            style = unsafeJso {
                height = 24.px
                width = 24.px
                marginRight = 12.px
            }
            viewBox = "0 0 122.8 122.8"
            path {
                d =
                    "M25.8 77.6c0 7.1-5.8 12.9-12.9 12.9S0 84.7 0 77.6s5.8-12.9 12.9-12.9h12.9v12.9zm6.5 0c0-7.1 5.8-12.9 12.9-12.9s12.9 5.8 12.9 12.9v32.3c0 7.1-5.8 12.9-12.9 12.9s-12.9-5.8-12.9-12.9V77.6z"
                fill = "#e01e5a"
            }
            path {
                d =
                    "M45.2 25.8c-7.1 0-12.9-5.8-12.9-12.9S38.1 0 45.2 0s12.9 5.8 12.9 12.9v12.9H45.2zm0 6.5c7.1 0 12.9 5.8 12.9 12.9s-5.8 12.9-12.9 12.9H12.9C5.8 58.1 0 52.3 0 45.2s5.8-12.9 12.9-12.9h32.3z"
                fill = "#36c5f0"
            }
            path {
                d =
                    "M97 45.2c0-7.1 5.8-12.9 12.9-12.9s12.9 5.8 12.9 12.9-5.8 12.9-12.9 12.9H97V45.2zm-6.5 0c0 7.1-5.8 12.9-12.9 12.9s-12.9-5.8-12.9-12.9V12.9C64.7 5.8 70.5 0 77.6 0s12.9 5.8 12.9 12.9v32.3z"
                fill = "#2eb67d"
            }
            path {
                d =
                    "M77.6 97c7.1 0 12.9 5.8 12.9 12.9s-5.8 12.9-12.9 12.9-12.9-5.8-12.9-12.9V97h12.9zm0-6.5c-7.1 0-12.9-5.8-12.9-12.9s5.8-12.9 12.9-12.9h32.3c7.1 0 12.9 5.8 12.9 12.9s-5.8 12.9-12.9 12.9H77.6z"
                fill = "#ecb22e"
            }
        }
        +"Add to Slack"
    }
}
