package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.img

val GqlButton by nfc<Props> {
    CouplingLink {
        to = "/graphiql"
        ariaLabel = "GraphiQL"
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
