package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import web.cssom.Display
import web.cssom.FontWeight
import web.cssom.px

val PairAssignmentBlock by nfc<PropsWithChildren> { props ->
    div {
        css {
            display = Display.inlineBlock
            fontSize = 28.px
            fontWeight = FontWeight.bold
            borderRadius = 15.px
            paddingLeft = 40.px
            paddingRight = 5.px
            paddingBottom = 6.px
        }
        +props.children
    }
}
