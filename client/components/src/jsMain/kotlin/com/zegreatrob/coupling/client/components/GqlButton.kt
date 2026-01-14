package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.img
import tanstack.react.router.Link
import tanstack.router.core.RoutePath

val GqlButton by nfc<Props> {
    Link {
        to = RoutePath("/graphiql")
        tabIndex = -1
        draggable = false
        CouplingButton {
            sizeRuleSet = large
            colorRuleSet = white
            img {
                src = CouplingImages.images.graphqlSvg
                height = 18.0
                width = 18.0
            }
        }
    }
}
