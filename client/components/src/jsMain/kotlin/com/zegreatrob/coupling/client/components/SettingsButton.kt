package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.css.ClassName
import emotion.react.css
import react.dom.html.ReactHTML.i
import react.router.dom.Link
import web.cssom.ClassName
import web.cssom.Padding
import web.cssom.px

@ReactFunc
val SettingsButton by nfc<PartyButtonProps> { props ->
    Link {
        to = "/${props.partyId.value}/edit"
        tabIndex = -1
        draggable = false
        CouplingButton(
            large,
            black,
            ClassName {
                fontSize = 24.px
                padding = Padding(1.px, 4.px, 2.px)
                "i" {
                    margin = 0.px
                }
            },
        ) {
            i { css(ClassName("fa fa-cog")) {} }
        }
    }
}
