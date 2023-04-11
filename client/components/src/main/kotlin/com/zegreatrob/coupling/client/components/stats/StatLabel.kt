package com.zegreatrob.coupling.client.components.stats

import csstype.px
import emotion.react.css
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.span

val StatLabel = FC<PropsWithChildren> { props ->
    span {
        css {
            marginRight = 5.px
        }
        +props.children
    }
}
