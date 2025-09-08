package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.PropsWithChildren
import react.dom.html.ReactHTML
import web.cssom.Display
import web.cssom.FontWeight
import web.cssom.px

val PairAssignmentBlock by nfc<PropsWithChildren> { props ->
    ReactHTML.div {
        css {
            display = Display.Companion.inlineBlock
            fontSize = 28.px
            fontWeight = FontWeight.Companion.bold
            borderRadius = 15.px
            paddingLeft = 40.px
            paddingRight = 5.px
            paddingBottom = 6.px
        }
        +props.children
    }
}
