package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.nfc
import csstype.Display
import csstype.FontWeight
import csstype.px
import emotion.react.css
import react.PropsWithChildren
import react.dom.html.ReactHTML.div

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
