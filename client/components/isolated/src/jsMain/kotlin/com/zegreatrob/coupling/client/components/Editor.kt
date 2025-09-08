package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.PropsWithChildren
import react.dom.html.ReactHTML.ul
import web.cssom.Border
import web.cssom.BoxSizing
import web.cssom.Clear
import web.cssom.Color
import web.cssom.Display
import web.cssom.Float
import web.cssom.FontSize
import web.cssom.LineStyle
import web.cssom.Margin
import web.cssom.None
import web.cssom.Overflow
import web.cssom.Padding
import web.cssom.TextAlign
import web.cssom.VerticalAlign
import web.cssom.pct
import web.cssom.px
import web.cssom.string

val Editor by nfc<PropsWithChildren> { props ->
    ul {
        css {
            verticalAlign = VerticalAlign.top
            fontSize = FontSize.large
            padding = Padding(0.px, 20.px, 0.px, 10.px)

            "ul" {
                listStyle = None.none
                padding = 0.px
                margin = 0.px
            }
            "li" {
                display = Display.block
                padding = 9.px
                border = Border(1.px, LineStyle.solid, Color("#F0D175FF"))
                marginBottom = 30.px
                borderRadius = 3.px
            }
            "li:last-child" {
                marginBottom = 0.px
                textAlign = TextAlign.center
            }
            "li > label" {
                display = Display.block
                float = Float.left
                marginTop = (-19).px
                backgroundColor = Color("#FDF9EDFF")
                height = 14.px
                padding = Padding(2.px, 5.px, 2.px, 5.px)
                color = Color("#CDA018FF")
                fontSize = 14.px
                overflow = Overflow.hidden
                fontFamily = string("Arial, Helvetica, sans-serif")
            }
            """ input[type="text"],
                input[type="date"],
                input[type="datetime"],
                input[type="email"],
                input[type="number"],
                input[type="search"],
                input[type="time"],
                input[type="url"],
                input[type="password"],
                textarea,
                select  """ {
                boxSizing = BoxSizing.borderBox
                asDynamic()["-webkit-box-sizing"] = "border-box"
                asDynamic()["-moz-box-sizing"] = "border-box"
                width = 100.pct
                display = Display.block
                outline = None.none
                border = None.none
                height = 25.px
                lineHeight = 25.px
                fontSize = 16.px
                padding = 0.px
                backgroundColor = Color("#FDF9EDFF")
            }

            "li > span" {
                backgroundColor = Color("#FBF3DAFF")
                display = Display.block
                padding = 3.px
                margin = Margin(0.px, (-9).px, (-9).px, (-9).px)
                textAlign = TextAlign.center
                color = Color("#CDA018FF")
                fontSize = 11.px
            }

            "textarea" {
                resize = None.none
            }
            """
                input[type="submit"],
                input[type="button"] 
            """ {
                backgroundColor = Color("#2471FF")
                border = None.none
                padding = Padding(10.px, 20.px, 10.px, 20.px)
                borderBottom = Border(3.px, LineStyle.solid, Color("#5994FF"))
                borderRadius = 3.px
                color = Color("#D2E2FF")
            }
            """
                input[type="submit"]:hover,
                input[type="button"]:hover 
            """ {
                backgroundColor = Color("#6B9FFF")
                color = Color("#fff")
            }
            "div" {
                clear = Clear.both
            }
        }
        +props.children
    }
}
