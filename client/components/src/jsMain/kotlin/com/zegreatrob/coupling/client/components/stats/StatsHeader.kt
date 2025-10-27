package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.PropsWithChildren
import react.dom.html.ReactHTML
import web.cssom.FontSize
import web.cssom.FontWeight
import web.cssom.px

val StatsHeader by nfc<PropsWithChildren> { props ->
    ReactHTML.div {
        css {
            fontWeight = FontWeight.bold
            fontSize = FontSize.large
            marginBottom = 5.px
        }
        +props.children
    }
}
