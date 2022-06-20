package com.zegreatrob.coupling.client.stats

import csstype.FontSize
import csstype.FontWeight
import csstype.px
import emotion.react.css
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.div

val StatsHeader = FC<PropsWithChildren> { props ->
    div {
        css {
            fontWeight = FontWeight.bold
            fontSize = FontSize.large
            marginBottom = 5.px
        }
        +props.children
    }
}
