package com.zegreatrob.coupling.client.components

import emotion.react.css
import react.FC
import react.Props
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.i
import web.cssom.ClassName
import web.cssom.Globals
import web.cssom.None
import web.cssom.em

external interface CloseButtonProps : Props {
    var onClose: () -> Unit
}

val CloseButton = FC<CloseButtonProps> { props ->
    button {
        css {
            color = Globals.inherit
            border = None.none
            padding = 0.em
            backgroundColor = Globals.inherit
        }
        ariaLabel = "Close"
        i { className = ClassName("fa fa-close") }
        onClick = { props.onClose() }
    }
}
