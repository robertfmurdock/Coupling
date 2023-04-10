package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.add
import react.FC
import react.Props
import react.dom.html.ReactHTML.img
import react.router.dom.Link

val GqlButton = FC<Props> {
    Link {
        to = "/graphiql"
        tabIndex = -1
        draggable = false
        add(
            com.zegreatrob.coupling.client.components.CouplingButton(
                com.zegreatrob.coupling.client.components.large,
                com.zegreatrob.coupling.client.components.white,
            ),
        ) {
            img {
                src = svgPath("graphql")
                height = 18.0
                width = 18.0
            }
        }
    }
}
