package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.PropsWithChildren
import react.dom.html.ReactHTML.span
import web.cssom.px

val StatLabel by nfc<PropsWithChildren> { props ->
    span {
        css {
            marginRight = 5.px
        }
        +props.children
    }
}
