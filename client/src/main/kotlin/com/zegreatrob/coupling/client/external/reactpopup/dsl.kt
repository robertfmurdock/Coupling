package com.zegreatrob.coupling.client.external.reactpopup

import com.zegreatrob.coupling.client.external.react.reactElement
import react.RBuilder
import styled.StyledHandler
import styled.styled
import kotlin.js.Json

private val styledPopup = styled(default)

fun RBuilder.popup(
    trigger: RBuilder.(Boolean) -> Unit,
    modal: Boolean,
    on: Array<String>,
    open: Boolean? = false,
    handler: StyledHandler<PopupProps>,
    contentStyle: Json
) = styledPopup {
    attrs {
        this.modal = modal
        this.on = on
        this.open = open
        this.trigger = { isOpen -> reactElement { trigger(isOpen) } }
        this.contentStyle = contentStyle
    }
    handler()
}
