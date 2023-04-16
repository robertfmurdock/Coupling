package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.minreact.nfc
import csstype.FontSize
import csstype.FontWeight
import csstype.px
import emotion.react.css
import react.PropsWithChildren
import react.dom.html.ReactHTML.div

val StatsHeader by nfc<PropsWithChildren> { props ->
    div {
        css {
            fontWeight = FontWeight.bold
            fontSize = FontSize.large
            marginBottom = 5.px
        }
        +props.children
    }
}
