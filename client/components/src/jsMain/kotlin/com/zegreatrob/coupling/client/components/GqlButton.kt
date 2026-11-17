package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.img
import react.router.dom.Link

val GqlButton by nfc<Props> {
    Link {
        to = "/graphiql"
        ariaLabel = "GraphiQL"
        tabIndex = -1
        draggable = false
        CouplingButton {
            sizeRuleSet = large
            colorRuleSet = white
            img {
                src = CouplingImages.images.graphqlSvg
                alt = ""
                height = 18.0
                width = 18.0
            }
        }
    }
}
