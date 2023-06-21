package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import emotion.css.ClassName
import react.PropsWithChildren
import web.cssom.Color
import web.cssom.VerticalAlign
import web.cssom.px

val PartyListFrame by nfc<PropsWithChildren> { props ->
    add(
        PageFrame(
            borderColor = Color("rgb(94, 84, 102)"),
            backgroundColor = Color("hsla(0, 0%, 80%, 1)"),
            className = ClassName {
                "> div" { padding = 7.px }
                "*" { verticalAlign = VerticalAlign.middle }
            },
        ),
    ) {
        +props.children
    }
}
