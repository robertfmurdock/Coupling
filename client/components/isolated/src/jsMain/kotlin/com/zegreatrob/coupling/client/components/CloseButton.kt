package com.zegreatrob.coupling.client.components

import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML
import web.cssom.ClassName
import web.cssom.Globals
import web.cssom.None
import web.cssom.em

external interface CloseButtonProps : Props {
    var onClose: () -> Unit
}

val CloseButton = FC<CloseButtonProps> { props ->
    ReactHTML.button {
        css {
            color = Globals.Companion.inherit
            border = None.Companion.none
            padding = 0.em
            backgroundColor = Globals.Companion.inherit
        }
        ariaLabel = "Close"
        ReactHTML.i { className = ClassName("fa fa-close") }
        onClick = { props.onClose() }
    }
}
