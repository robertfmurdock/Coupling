package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.PropsWithChildren
import react.dom.html.ReactHTML
import web.cssom.px

val StatLabel by nfc<PropsWithChildren> { props ->
    ReactHTML.span {
        css {
            marginRight = 5.px
        }
        +props.children
    }
}
