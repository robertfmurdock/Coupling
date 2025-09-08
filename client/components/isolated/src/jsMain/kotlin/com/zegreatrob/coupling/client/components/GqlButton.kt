package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML
import react.router.dom.Link

val GqlButton by nfc<Props> {
    Link {
        to = "/graphiql"
        tabIndex = -1
        draggable = false
        CouplingButton {
            sizeRuleSet = large
            colorRuleSet = white
            ReactHTML.img {
                src = svgPath("graphql")
                height = 18.0
                width = 18.0
            }
        }
    }
}
