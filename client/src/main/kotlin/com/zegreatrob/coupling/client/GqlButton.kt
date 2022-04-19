package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.white
import com.zegreatrob.minreact.child
import react.FC
import react.Props
import react.dom.html.ReactHTML.img
import react.router.dom.Link

val GqlButton = FC<Props> {
    Link {
        to = "/graphiql"
        tabIndex = -1
        draggable = false
        child(CouplingButton(large, white)) {
            img {
                src = svgPath("graphql")
                height = 18.0
                width = 18.0
            }
        }
    }
}
