package com.zegreatrob.coupling.client.external.reactpopup

import react.ChildrenBuilder
import react.Fragment
import react.ReactNode
import react.create
import kotlin.js.Json

fun popup(
    trigger: (Boolean) -> ReactNode,
    modal: Boolean,
    on: Array<String>,
    open: Boolean? = false,
    handler: ChildrenBuilder.() -> Unit,
    contentStyle: Json? = null,
    onOpen: () -> Unit = {},
    onClose: () -> Unit = {},
) = default.create {
    this.modal = modal
    this.on = on
    this.open = open
    this.trigger = { isOpen -> trigger(isOpen) }
    this.contentStyle = contentStyle
    this.onOpen = onOpen
    this.onClose = onClose
    +Fragment.create(handler)
}.also {
    loadDefaultCss()
}

private fun loadDefaultCss() {
    js("require('reactjs-popup/dist/index.css')")
}
