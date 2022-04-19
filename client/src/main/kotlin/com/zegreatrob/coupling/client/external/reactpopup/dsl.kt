package com.zegreatrob.coupling.client.external.reactpopup

import react.ChildrenBuilder
import react.Fragment
import react.ReactNode
import react.buildElement
import react.create
import styled.styled
import kotlin.js.Json

private val styledPopup = styled(default)

fun popup(
    trigger: (Boolean) -> ReactNode,
    modal: Boolean,
    on: Array<String>,
    open: Boolean? = false,
    handler: ChildrenBuilder.() -> Unit,
    contentStyle: Json? = null,
    onOpen: () -> Unit = {},
    onClose: () -> Unit = {},
) = buildElement {
    styledPopup {
        attrs {
            this.modal = modal
            this.on = on
            this.open = open
            this.trigger = { isOpen -> trigger(isOpen) }
            this.contentStyle = contentStyle
            this.onOpen = onOpen
            this.onClose = onClose
        }
        +Fragment.create(handler)
    }.also {
        loadDefaultCss()
    }
}

private fun loadDefaultCss() {
    js("require('reactjs-popup/dist/index.css')")
}
