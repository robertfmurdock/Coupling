package com.zegreatrob.coupling.client.components

import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML
import web.cssom.ClassName
import web.cssom.Cursor
import web.cssom.Globals
import web.cssom.None
import web.cssom.em
import web.cssom.rgb
import web.html.ButtonType
import web.html.button

external interface CloseButtonProps : Props {
    var onClose: () -> Unit
    var autoFocus: Boolean?
}

val CloseButton = FC<CloseButtonProps> { props ->
    ReactHTML.button {
        type = ButtonType.button
        autoFocus = props.autoFocus
        css {
            color = Globals.inherit
            border = None.none
            padding = 0.em
            backgroundColor = Globals.inherit
            cursor = Cursor.pointer
            borderRadius = 0.2.em
            hover {
                backgroundColor = rgb(0, 0, 0, 0.12)
            }
        }
        ariaLabel = "Close"
        ReactHTML.i { className = ClassName("fa fa-close") }
        onClick = { props.onClose() }
    }
}
