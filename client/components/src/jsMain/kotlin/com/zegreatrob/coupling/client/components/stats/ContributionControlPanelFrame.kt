package com.zegreatrob.coupling.client.components.stats

import emotion.react.css
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import web.cssom.AlignItems
import web.cssom.Display
import web.cssom.FontSize
import web.cssom.fr
import web.cssom.px

val ContributionControlPanelFrame = FC<PropsWithChildren> { props ->
    div {
        css {
            margin = 6.px
            display = Display.grid
            fontSize = FontSize.smaller
            gridTemplateColumns = web.cssom.repeat(2, 1.fr)
            alignItems = AlignItems.baseline
        }
        +props.children
    }
}
