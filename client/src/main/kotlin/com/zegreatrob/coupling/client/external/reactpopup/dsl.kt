package com.zegreatrob.coupling.client.external.reactpopup

import react.ReactElement
import react.buildElement
import styled.StyledHandler
import styled.styled
import kotlin.js.Json

private val styledPopup = styled(default)

fun popup(
    trigger: (Boolean) -> ReactElement,
    modal: Boolean,
    on: Array<String>,
    open: Boolean? = false,
    handler: StyledHandler<PopupProps>,
    contentStyle: Json? = null
) = buildElement {
    styledPopup {
        attrs {
            this.modal = modal
            this.on = on
            this.open = open
            this.trigger = { isOpen -> trigger(isOpen) }
            this.contentStyle = contentStyle
        }
        handler()
    }.also {
        loadDefaultCss()
    }
}

private fun loadDefaultCss() {
    js("require('reactjs-popup/dist/index.css')")
}
